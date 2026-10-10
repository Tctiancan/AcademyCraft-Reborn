package org.academy.internal.client.render.vfx;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import org.academy.api.client.render.vfx.VfxRenderContext;
import org.academy.api.client.render.vfx.VfxRenderer;
import org.academy.api.client.util.VertexUtil;
import org.joml.Matrix4f;

import java.util.List;

/**
 * 1.21.1 实装 (原 26.2 光线实体)。
 *
 * <p>26.2 通过 GpuDevice/实例化管线 (每束一个 64 字节实例变换, icosphere 球 + 圆柱盒) 延迟提交;
 * 1.21.1 无 GPU 实例化 API, 改为在 CPU 端逐束将几何顶点经实例矩阵变换到世界空间, 再以
 * {@code position_color} shader 即时冲刷。颜色: {@code glow} 管为绿色调制, 否则白色。
 */
public final class BeamRenderer implements VfxRenderer<BeamData> {
    private static final float[][] ICOSPHERE = VertexUtil.Ball.getIcosphereVertexBuffer(1.0f, 2, true);
    private static final float[][] CYLINDER = VertexUtil.Cylinder.getCylinderVertexBuffer(0.0f, 1.0f, 0.5f, 16, false);

    private final boolean glow;
    private final float[] color;
    private final float ballFactor;
    private final float boxFactorXZ;

    public BeamRenderer(boolean glow) {
        this.glow = glow;
        this.color = glow ? new float[]{0.0f, 1.0f, 0.0f, 1.0f} : new float[]{1.0f, 1.0f, 1.0f, 1.0f};
        if (glow) {
            ballFactor = 1.0f;
            boxFactorXZ = 1.0f;
        } else {
            ballFactor = 0.85f;
            boxFactorXZ = 0.75f;
        }
    }

    @Override
    public void init() {
    }

    @Override
    public void close() {
    }

    @Override
    public void render(VfxRenderContext ctx, List<? extends BeamData> data) {
        if (data.isEmpty()) {
            return;
        }
        var cameraPos = ctx.cameraPos();
        var viewMatrix = new Matrix4f(ctx.viewRotationMatrix())
                .translate(-cameraPos.x(), -cameraPos.y(), -cameraPos.z());

        RenderSystem.enableBlend();
        if (glow) {
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        } else {
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        }
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();

        var modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushMatrix();
        modelViewStack.set(viewMatrix);
        RenderSystem.applyModelViewMatrix();

        // renderLevel RETURN 时 RenderSystem 的环境投影不可靠，显式用帧捕获的真实世界透视投影设置。
        var prevProjection = RenderSystem.getProjectionMatrix();

        // 注意：1.21.1 的 Tesselator.begin() 每次返回共享同一 ByteBufferBuilder 的新 BufferBuilder。
        // 若同时创建 ball/box 两个 builder 交错写入，build() 会一次性取走两段字节但 drawState 只按
        // 各自 builder 的顶点数绘制，圆柱轴数据会被丢弃导致光束不可见。因此必须拆成先后两个独立 pass：
        // 先构建并冲刷球网格 (清空共享缓冲)，再重建圆柱 builder 写入并冲刷。
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        var instance = new Matrix4f();
        var ballBuilder = Tesselator.getInstance().begin(
                VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        for (var beam : data) {
            var ballRadius = beam.progress() * 0.185f * beam.ballScale();
            instance.identity()
                    .translate(beam.pos())
                    .rotateY((90 - beam.yRot()) * Mth.DEG_TO_RAD)
                    .rotateZ((90 + beam.xRot()) * Mth.DEG_TO_RAD)
                    .scale(ballRadius * ballFactor);
            appendIcosphere(ballBuilder, instance);
        }
        MeshData ballMesh = ballBuilder.build();
        if (ballMesh != null) {
            BufferUploader.drawWithShader(ballMesh);
        }

        var boxBuilder = Tesselator.getInstance().begin(
                VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        for (var beam : data) {
            var rayProgress = beam.isCharging() ? 0.0f : beam.progress();
            var width = rayProgress * 0.25f * boxFactorXZ * beam.widthScale();
            instance.identity()
                    .translate(beam.pos())
                    .rotateY((90 - beam.yRot()) * Mth.DEG_TO_RAD)
                    .rotateZ((90 + beam.xRot()) * Mth.DEG_TO_RAD)
                    .scale(width, beam.length(), width);
            appendCylinderQuads(boxBuilder, instance);
        }
        MeshData boxMesh = boxBuilder.build();
        if (boxMesh != null) {
            BufferUploader.drawWithShader(boxMesh);
        }

        modelViewStack.popMatrix();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.setProjectionMatrix(prevProjection, com.mojang.blaze3d.vertex.VertexSorting.DISTANCE_TO_ORIGIN);
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
    }

    private void appendIcosphere(VertexConsumer consumer, Matrix4f instance) {
        for (var vertex : ICOSPHERE) {
            consumer.addVertex(instance, vertex[0], vertex[1], vertex[2])
                    .setColor(color[0], color[1], color[2], color[3]);
        }
    }

    private void appendCylinderQuads(VertexConsumer consumer, Matrix4f instance) {
        for (var i = 0; i < CYLINDER.length; i += 4) {
            var a = CYLINDER[i];
            var b = CYLINDER[i + 1];
            var c = CYLINDER[i + 2];
            var d = CYLINDER[i + 3];
            addVertex(consumer, instance, a);
            addVertex(consumer, instance, b);
            addVertex(consumer, instance, c);
            addVertex(consumer, instance, a);
            addVertex(consumer, instance, c);
            addVertex(consumer, instance, d);
        }
    }

    private void addVertex(VertexConsumer consumer, Matrix4f instance, float[] v) {
        consumer.addVertex(instance, v[0], v[1], v[2])
                .setColor(color[0], color[1], color[2], color[3]);
    }
}
