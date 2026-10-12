package org.academy.internal.client.render.vfx;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.GameRenderer;
import org.academy.api.client.render.vfx.VfxRenderContext;
import org.academy.api.client.render.vfx.VfxRenderer;
import org.joml.Matrix4f;

import java.util.List;

/**
 * 1.21.1 实装 (原 26.2 闪电管状网格)。
 *
 * <p>原 26.2 用自定义 {@code vfx_lightning} shader 计算管身颜色:
 * {@code intensity = uv.x}, {@code color = base + emission * intensity},
 * glow 时对每通道做 ACES 色调映射, 透明度为 OPACITY, 顶点在 shader 内先减相机偏移再乘视旋转。
 * 1.21.1 无该自定义 uniform 管线, 改为在 CPU 端把每顶点的最终颜色烘焙进
 * {@code POSITION_COLOR}, 以同样的 "相机-相对位置 + 视旋转矩阵" 即时冲刷。
 */
public final class LightningRenderer implements VfxRenderer<LightningMeshData> {
    private static final float BASE_R = 0.059f;
    private static final float BASE_G = 0.224f;
    private static final float BASE_B = 0.710f;
    private static final float EMISSION_R = 0.80f;
    private static final float EMISSION_G = 4.0f;
    private static final float EMISSION_B = 10.0f;
    private static final float OPACITY = 0.4f;

    private final boolean glow;

    public LightningRenderer(boolean glow) {
        this.glow = glow;
    }

    @Override
    public void init() {
    }

    @Override
    public void render(VfxRenderContext ctx, List<? extends LightningMeshData> data) {
        if (data.isEmpty()) {
            return;
        }
        var cameraPos = ctx.cameraPos();
        var viewMatrix = new Matrix4f(ctx.viewRotationMatrix());

        RenderSystem.enableBlend();
        if (glow) {
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        } else {
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        }
        RenderSystem.disableDepthTest();
        // 26.2 的 LIGHTNING_TUBE pipeline 明确 withCull(false): 电弧管状网格是闭合曲面,
        // 其绕序会让朝向相机的面被视作背面而整根被剔除。1.21.1 移植此前漏配 cull 状态,
        // 导致网格即使构建成功也完全不可见。此处开/关包住整个绘制循环并恢复。
        RenderSystem.disableCull();

        var modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushMatrix();
        modelViewStack.set(viewMatrix);
        RenderSystem.applyModelViewMatrix();

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        for (var item : data) {
            var mesh = item.tube().mesh();
            if (mesh.isEmpty()) {
                continue;
            }
            var positions = mesh.positions();
            var uvs = mesh.uvs();
            var indices = mesh.indices();
            var vertexCount = mesh.vertexCount();

            var builder = Tesselator.getInstance().begin(
                    VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
            appendMesh(builder, positions, uvs, indices, vertexCount, cameraPos.x(), cameraPos.y(), cameraPos.z());
            MeshData meshData = builder.build();
            if (meshData != null) {
                BufferUploader.drawWithShader(meshData);
            }
        }

        modelViewStack.popMatrix();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
    }

    private void appendMesh(VertexConsumer consumer, float[] positions, float[] uvs,
                            int[] indices, int vertexCount,
                            float camX, float camY, float camZ) {
        if (indices.length == 0) {
            for (var k = 0; k < vertexCount; k++) {
                appendVertex(consumer, positions, uvs, k, camX, camY, camZ);
            }
            return;
        }
        for (var idx : indices) {
            appendVertex(consumer, positions, uvs, idx, camX, camY, camZ);
        }
    }

    private void appendVertex(VertexConsumer consumer, float[] positions, float[] uvs,
                              int k, float camX, float camY, float camZ) {
        var p = k * 3;
        var u = k * 2;
        float intensity = uvs[u];
        float r = BASE_R + EMISSION_R * intensity;
        float g = BASE_G + EMISSION_G * intensity;
        float b = BASE_B + EMISSION_B * intensity;
        if (glow) {
            r = aces(r);
            g = aces(g);
            b = aces(b);
        }
        consumer.addVertex(positions[p] - camX, positions[p + 1] - camY, positions[p + 2] - camZ)
                .setColor(r, g, b, OPACITY);
    }

    private static float aces(float x) {
        return Math.max(0.0f, Math.min(1.0f,
                (x * (2.51f * x + 0.03f)) / (x * (2.43f * x + 0.59f) + 0.14f)));
    }

    @Override
    public void close() {
    }
}
