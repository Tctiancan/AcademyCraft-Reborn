package org.academy.internal.client.ability.aeromanip;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import org.academy.AcademyCraft;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 * 移植自 Minecraft 26.2 的 FlowSense 空心标记渲染器, 适配 1.21.1 即时渲染管线.
 *
 * <p>26.2 版本通过 GPU 命令编码器把"共享四边形 + 每标记实例缓冲 + 屏幕空间扩展"一次性
 * 提交 (GpuBuffer / VertexFormat.builder / GpuCommandEncoder / RenderPipeline); 1.21.1 无这套
 * 即时态设备, 故本实现把每个标记在 CPU 侧解开为一张面向相机的四边形, 以相机相对坐标写入
 * {@link RenderType} 的 {@link com.mojang.blaze3d.vertex.BufferBuilder}, 由 {@code RenderSystem} 完成投影.
 *
 * <p>尺寸语义保持一致: 标记在屏幕上的像素大小由每标记的 {@code size} 决定 (与距离无关),
 * 颜色为半透明白青 (190,229,235), 深度无关 (不写深度、深度测试恒通过), 半透明混合.
 *
 * <p>GPU 探针 ({@link #beginGpuProfile()}, {@link #gpuSamples()}) 在 1.21.1 无对应的时间戳
 * 查询池, 保留符号但返回空结果, 仅作调试占位.
 */
//@EventBusSubscriber(modid = AcademyCraft.MOD_ID, value = Dist.CLIENT)
public final class FlowSenseRenderer implements AutoCloseable {
    /** 半透明、深度无关、面向相机的空心标记渲染状态 (位置+颜色, 四边形). */
    private static final RenderType MARKERS = RenderType.create(
            "academy_flow_sense_markers",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS,
            786432, false, false,
            RenderType.CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getPositionColorShader))
                    .setTransparencyState(new RenderStateShard.TransparencyStateShard("translucent_transparency",
                            () -> {
                                RenderSystem.enableBlend();
                                RenderSystem.defaultBlendFunc();
                            },
                            () -> RenderSystem.disableBlend()))
                    .setDepthTestState(new RenderStateShard.DepthTestStateShard("no_depth_test", 519))
                    .setWriteMaskState(new RenderStateShard.WriteMaskStateShard(true, false))
                    .setCullState(new RenderStateShard.CullStateShard(false))
                    .createCompositeState(false));

    private long lastNanos, prepareNanos, uploadNanos, frames;
    private int drawn;

    /** Explicit, bounded profiling hook; normal rendering allocates no timestamp queries. */
    public void beginGpuProfile() {
        // 1.21.1 无 GPU 时间戳查询池; 调试探针占位 (C-stun).
    }
    public java.util.List<Long> gpuSamples() {
        // 1.21.1 无 GPU 时间戳查询池; 返回空采样 (C-stun).
        return new java.util.ArrayList<>();
    }
    public long lastNanos() { return lastNanos; }
    public long prepareNanos() { return prepareNanos; }
    public long uploadNanos() { return uploadNanos; }
    public long frames() { return frames; }
    public int drawn() { return drawn; }

    public void render(FlowObservationCache cache, long tick, Vec3 camera, Matrix4fc view, Matrix4fc projection) {
        long start = System.nanoTime();
        var mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        drawn = 0;
        float partial = mc.getTimer().getGameTimeDeltaPartialTick(false);
        var matrix = new Matrix4f(projection).mul(view);
        var fullInv = new Matrix4f(matrix).invert();
        var clip = new Vector4f();
        var win = mc.getWindow();
        float winW = Math.max(1, win.getWidth());
        float winH = Math.max(1, win.getHeight());
        var bufferSource = mc.renderBuffers().bufferSource();
        VertexConsumer builder = bufferSource.getBuffer(MARKERS);
        for (var marker : cache.markers()) {
            if (marker.expiresAt <= tick) continue;
            var entity = mc.level.getEntity(marker.sample.id());
            double x, y, z;
            if (entity != null) {
                // Life/skill eligibility is authoritative in the observation. Avoid invoking the
                // full health/defense hook chain
// for every observed entity on every rendered frame.
                if (!entity.getUUID().equals(marker.sample.uuid()) || entity.isRemoved()) continue;
                if (entity.distanceToSqr(mc.player) > cache.range() * cache.range()) continue;
                var position = entity.getPosition(partial);
                x = position.x - camera.x; y = position.y + entity.getBbHeight() * .5 - camera.y; z = position.z - camera.z;
            } else {
                var position = marker.position(tick + partial);
                if (position.distanceToSqr(mc.player.position()) > (cache.range() + 2) * (cache.range() + 2)) continue;
                x = position.x - camera.x; y = position.y - camera.y; z = position.z - camera.z;
            }
            clip.set((float) x, (float) y, (float) z, 1).mul(matrix);
            if (clip.w <= 0 || Math.abs(clip.x) > clip.w * 1.05f || Math.abs(clip.y) > clip.w * 1.05f) continue;
            float size = (float) Math.clamp(240 * marker.sample.width() / Math.max(1, Math.sqrt(x * x + y * y + z * z)), 5, 18);
            float alpha = (float) Math.min(.68, Math.min((tick + partial - marker.bornAt) / 4,
                    (marker.expiresAt - tick - partial) / 8));
            emitMarker(builder, matrix, fullInv, (float) x, (float) y, (float) z, size, alpha, winW, winH);
            drawn++;
        }
        prepareNanos = System.nanoTime() - start;
        if (drawn == 0) { lastNanos = prepareNanos; uploadNanos = 0; return; }
        // 以相机旋转矩阵作为模型矩阵; 顶点以相机相对坐标给出, 由 RenderSystem 完成投影.
        var modelView = RenderSystem.getModelViewStack();
        modelView.pushMatrix();
        modelView.set(view);
        RenderSystem.applyModelViewMatrix();
        bufferSource.endBatch(MARKERS);
        modelView.popMatrix();
        uploadNanos = System.nanoTime() - start - prepareNanos;
        frames++; lastNanos = System.nanoTime() - start;
    }

    /**
     * 把单个标记展开为一张面向相机的四边形, 其屏幕像素尺寸恒为 {@code size}.
     *
     * <p>算法: 先将标记中心投影到 NDC, 加上 「size/窗口尺寸」 对应的 NDC 偏移得到四个角点
     * 的 NDC 坐标, 再用逆矩阵反解回相机空间顶点. 由于 NDC 偏移与距离无关, 像素大小恒定.
     */
    private static void emitMarker(VertexConsumer builder, Matrix4fc matrix, Matrix4fc fullInv,
                                   float cx, float cy, float cz, float size, float alpha,
                                   float winW, float winH) {
        var clipTmp = new Vector4f(cx, cy, cz, 1f).mul(matrix);
        float w = clipTmp.w;
        if (w <= 0) return;
        float ndcX = clipTmp.x / w;
        float ndcY = clipTmp.y / w;
        float ndcZ = clipTmp.z / w;
        float sx = size * 2f / winW;
        float sy = size * 2f / winH;
        float r = 190 / 255f, g = 229 / 255f, b = 235 / 255f;
        var tl = fullInv.transformPosition(new Vector3f(ndcX - sx, ndcY + sy, ndcZ), new Vector3f());
        var tr = fullInv.transformPosition(new Vector3f(ndcX + sx, ndcY + sy, ndcZ), new Vector3f());
        var br = fullInv.transformPosition(new Vector3f(ndcX + sx, ndcY - sy, ndcZ), new Vector3f());
        var bl = fullInv.transformPosition(new Vector3f(ndcX - sx, ndcY - sy, ndcZ), new Vector3f());
        builder.addVertex(tl.x, tl.y, tl.z).setColor(r, g, b, alpha);
        builder.addVertex(tr.x, tr.y, tr.z).setColor(r, g, b, alpha);
        builder.addVertex(br.x, br.y, br.z).setColor(r, g, b, alpha);
        builder.addVertex(bl.x, bl.y, bl.z).setColor(r, g, b, alpha);
    }

    @Override public void close() {
        prepareNanos = uploadNanos = lastNanos = frames = 0;
        drawn = 0;
    }
}
