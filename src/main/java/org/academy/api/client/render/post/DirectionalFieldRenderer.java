package org.academy.api.client.render.post;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;
import org.academy.api.common.vfx.DirectionalArea;
import org.academy.internal.client.render.AcademyShaders;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.GL20;

import java.util.List;

/**
 * 1.21.1 实装 (原 26.2 屏幕空间方向场散射, C-stun 替代)。
 *
 * <p>26.2 通过命令编码器 + std140 UBO + WorldSurfaceMasks 生成的几何/实体遮罩 + 三阶段
 * {@code directional_field_surface/volume/composite} 完成表面照明与半分辨率空气散射。1.21.1 无
 * {@code FeatureRenderDispatcher/ChunkSectionsToRender/GpuTextureView/Std140Builder} 等 GPU 延迟
 * 重建构件, ubo/batch 驱动路径不可移植。
 *
 * <p>本移植忠实保留其视觉语义并以 1.21.1 可用的两层管线复现：
 * <ol>
 *   <li>{@code directional_field} 单程融合排序阶段 — 从主帧缓冲深度纹理重建世界位置, 一趟同时计算
 *       「表面覆盖度」(surface) 与「球穿越区间空气散射/光线束」(volume), 以普通 uniform 数组携带
 *       至多 16 个方向场, 把半透明白色散射色调写入离屏只读 =场遮罩目标;</li>
 *   <li>复用 {@code screen_blit} 把该遮罩以半透混合合成回主帧缓冲。</li>
 * </ol>
 * 复用 {@link PostEffect} 确认的「离屏写/主帧读、主帧写/离屏读」分离避免帧缓冲反馈而破坏渲染目标的
 * 手法, 且主帧深度纹理从未被动过, 不影响首称手部渲染。生命周期 (render/renderedFrames/close) 不变。
 */
public final class DirectionalFieldRenderer {
    private static final int MAX_FIELDS = 16;
    private static final int GL_LINEAR = 9729;
    /** 场遮罩离屏目标: 存半透明白色 alpha, 供 screen_blit 合成回主帧。 */
    @Nullable
    private static RenderTarget fieldTarget;

    private static long renderedFrames;

    private DirectionalFieldRenderer() {
    }

    public static long renderedFrames() {
        return renderedFrames;
    }

    public static void render(Vec3 camera, Matrix4fc view, Matrix4fc projection, List<DirectionalArea> areas) {
        if (areas.isEmpty()) {
            close();
            return;
        }
        var shader = AcademyShaders.getDirectionalFieldShader();
        var blit = AcademyShaders.getScreenBlitShader();
        var mc = Minecraft.getInstance();
        if (shader == null || blit == null || mc.level == null) return;
        var main = mc.getMainRenderTarget();
        if (main == null) return;

        ensureTarget(main);
        var inverse = new Matrix4f(projection).mul(view).invert();
        if (!inverse.isFinite() || fieldTarget == null) return;

        // Pass A: 场遮罩 → FIELD, 采样主帧深度纹理。
        RenderSystem.disableBlend();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        fieldTarget.bindWrite(true);
        RenderSystem.setShader(() -> shader);
        // setShader 仅在 RenderSystem.shader 字段中暂存, 并不会绑定程序; 必须手动 apply() 让
        // 后续 glUniform* 作用到本程序, 否则 uniform 位置属于已被卸载的程序, 触发 GL_INVALID_OPERATION。
        shader.apply();
        setFieldUniforms(shader, camera, inverse, LevelContext.capture(mc, areas.getFirst()), areas);
        GL20.glUniform1i(GL20.glGetUniformLocation(getProgramId(shader), "SceneDepth"), 0);
        RenderSystem.setShaderTexture(0, main.getDepthTextureId());
        drawFullscreenQuad();
        // 离屏目标立即恢复为可读纹理目标, 绝不残留为帧内写目标。
        fieldTarget.bindRead();

        // Pass B: 半透混合合成回主帧缓冲。
        main.bindWrite(true);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(() -> blit);
        RenderSystem.setShaderTexture(0, fieldTarget.getColorTextureId());
        drawFullscreenQuad();

        // 恢复默认渲染状态, 避免影响随后手部渲染。
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        renderedFrames++;
    }

    /** 收集方向场 uniform 所需的环境/统一描边数据。 */
    private static final class LevelContext {
        final float light;
        final float sunAngle;
        final float time;

        LevelContext(float light, float sunAngle, float time) {
            this.light = light;
            this.sunAngle = sunAngle;
            this.time = time;
        }

        static LevelContext capture(Minecraft mc, DirectionalArea first) {
            var level = mc.level;
            var sample = BlockPos.containing(first.origin());
            int brightness = Math.max(level.getBrightness(LightLayer.BLOCK, sample),
                    Math.max(0, level.getBrightness(LightLayer.SKY, sample) - level.getSkyDarken()));
            return new LevelContext(brightness / 15f, level.getSunAngle(0.0f),
                    (level.getGameTime() % 24000) / 20f);
        }
    }

    private static void setFieldUniforms(
            ShaderInstance shader, Vec3 camera, Matrix4f inverse, LevelContext ctx, List<DirectionalArea> areas
    ) {
        int program = getProgramId(shader);
        int count = Math.min(MAX_FIELDS, areas.size());
        GL20.glUniformMatrix4fv(GL20.glGetUniformLocation(program, "InverseViewProjection"), false, inverse.get(new float[16]));
        GL20.glUniform4f(GL20.glGetUniformLocation(program, "SunLight"),
                (float) Math.sin(ctx.sunAngle), (float) Math.cos(ctx.sunAngle), 0, ctx.light);
        GL20.glUniform4f(GL20.glGetUniformLocation(program, "FieldParams"), ctx.time, count, 0, 0);
        GL20.glUniform4f(GL20.glGetUniformLocation(program, "NoiseAnchor"),
                (float) (camera.x % 4096), (float) (camera.y % 4096), (float) (camera.z % 4096), 0);
        for (int i = 0; i < MAX_FIELDS; i++) {
            if (i < count) {
                var a = areas.get(i);
                var p = a.origin().subtract(camera);
                GL20.glUniform4f(GL20.glGetUniformLocation(program, "FieldOrigins[" + i + "]"),
                        (float) p.x, (float) p.y, (float) p.z, (float) a.radius());
            } else {
                GL20.glUniform4f(GL20.glGetUniformLocation(program, "FieldOrigins[" + i + "]"), 0, 0, 0, 0);
            }
        }
        for (int i = 0; i < MAX_FIELDS; i++) {
            if (i < count) {
                var a = areas.get(i);
                var d = a.direction();
                GL20.glUniform4f(GL20.glGetUniformLocation(program, "FieldDirections[" + i + "]"),
                        (float) d.x, (float) d.y, (float) d.z, a.airVisibility(camera));
            } else {
                GL20.glUniform4f(GL20.glGetUniformLocation(program, "FieldDirections[" + i + "]"), 0, 0, 1, 1);
            }
        }
        for (int i = 0; i < MAX_FIELDS; i++) {
            if (i < count) {
                var a = areas.get(i);
                GL20.glUniform4f(GL20.glGetUniformLocation(program, "FieldCones[" + i + "]"),
                        (float) a.first().radius(), (float) a.first().minimumDot(),
                        (float) a.second().radius(), (float) a.second().minimumDot());
            } else {
                GL20.glUniform4f(GL20.glGetUniformLocation(program, "FieldCones[" + i + "]"), 0, 1, 0, 1);
            }
        }
    }

    private static int getProgramId(ShaderInstance shader) {
        return shader.getId();
    }

    private static void ensureTarget(RenderTarget main) {
        if (fieldTarget != null && fieldTarget.width == main.width && fieldTarget.height == main.height) return;
        if (fieldTarget != null) {
            fieldTarget.destroyBuffers();
            fieldTarget = null;
        }
        fieldTarget = new TextureTarget(main.width, main.height, false, Minecraft.ON_OSX);
        fieldTarget.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        fieldTarget.setFilterMode(GL_LINEAR);
    }

    /** 构建 NDC -1..1、POSITION 的全屏四边形 (screen_blit.vsh 忽略矩阵, 由 Position 直接输出)。 */
    private static void drawFullscreenQuad() {
        var allocator = new ByteBufferBuilder(512);
        var builder = new BufferBuilder(allocator, VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        builder.addVertex(-1.0F, -1.0F, 0.0F);
        builder.addVertex(1.0F, -1.0F, 0.0F);
        builder.addVertex(1.0F, 1.0F, 0.0F);
        builder.addVertex(-1.0F, 1.0F, 0.0F);
        var quad = builder.build();
        try {
            BufferUploader.drawWithShader(quad);
        } finally {
            quad.close();
        }
    }

    public static void close() {
        if (fieldTarget != null) {
            fieldTarget.destroyBuffers();
            fieldTarget = null;
        }
    }
}
