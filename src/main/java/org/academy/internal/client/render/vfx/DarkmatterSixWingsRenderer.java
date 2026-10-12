package org.academy.internal.client.render.vfx;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.GameRenderer;
import org.academy.api.client.render.vfx.VfxRenderContext;
import org.academy.api.client.render.vfx.VfxRenderer;
import org.academy.api.client.resources.R;
import org.joml.Matrix4f;

import java.nio.ByteOrder;
import java.util.List;

/**
 * 1.21.1 实装 (原 26.2 六翼暗物质特效)。
 *
 * <p>26.2 通过 GpuDevice/命令编码器以 MODEL_MESH 管线延迟提交; 1.21.1 无对应 API, 改用即时态提交:
 * 数据顶点为每 20 字节 (位置 3 浮点 + 纹理坐标 2 浮点) 的绝对世界坐标三角形流, 以相机-相对
 * 视旋转矩阵 (由 {@link VfxRenderContext#viewRotationMatrix()} 提供) 与 {@code RenderSystem}
 * 当前投影矩阵合成模型视图, 用 {@code positional_tex} shader 冲刷共享暗物质六翼贴图。
 */
public final class DarkmatterSixWingsRenderer implements VfxRenderer<DarkmatterSixWingsData> {
    @Override
    public void init() {
    }

    @Override
    public void render(VfxRenderContext ctx, List<? extends DarkmatterSixWingsData> data) {
        if (data.isEmpty()) {
            return;
        }
        var cameraPos = ctx.cameraPos();
        var viewMatrix = new Matrix4f(ctx.viewRotationMatrix())
                .translate(-cameraPos.x(), -cameraPos.y(), -cameraPos.z());

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.enableDepthTest();

        var modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushMatrix();
        modelViewStack.set(viewMatrix);
        RenderSystem.applyModelViewMatrix();

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, R.textures.darkmatter_six_wings_effect);
        for (var item : data) {
            if (item.vertexCount() <= 0) {
                continue;
            }
            var vertices = item.vertices().duplicate().order(ByteOrder.nativeOrder());
            vertices.limit(vertices.position() + item.vertexCount() * DarkmatterSixWingsVfx.VERTEX_STRIDE);
            var builder = Tesselator.getInstance().begin(
                    VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_TEX);
            while (vertices.hasRemaining()) {
                submitVertex(builder, vertices.getFloat(), vertices.getFloat(), vertices.getFloat(),
                        vertices.getFloat(), vertices.getFloat());
            }
            MeshData meshData = builder.build();
            if (meshData != null) {
                // 1.21.1: 立即态网格必须以 drawWithShader 提交, 携带 RenderSystem 当前
                // 模型视图/投影与 shader 状态; 裸 draw() 仅上传顶点而不合成分辨率像素。
                BufferUploader.drawWithShader(meshData);
            }
        }

        modelViewStack.popMatrix();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.enableDepthTest();
    }

    private static void submitVertex(VertexConsumer consumer, float x, float y, float z, float u, float v) {
        consumer.addVertex(x, y, z).setUv(u, v);
    }

    @Override
    public void close() {
    }
}
