package org.academy.internal.client.render.vfx;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.GameRenderer;
import org.academy.api.client.render.vfx.VfxRenderContext;
import org.academy.api.client.render.vfx.VfxRenderer;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.List;

/**
 * 1.21.1 实装 (原 26.2 带纹理世界四边形)。
 *
 * <p>26.2 通过 GpuDevice/命令编码器延迟提交; 1.21.1 无对应 API, 改用即时态提交:
 * 以相机-相对视旋转矩阵 (由 {@link VfxRenderContext#viewRotationMatrix()} 提供) 与 {@code RenderSystem}
 * 当前投影矩阵合成模型视图, 把世界空间角点以 {@code positional_tex_color} shader 冲刷到当前帧缓冲。
 * {@code glow} 为 true 时使用加法混合 (辉光)。
 */
public final class TexturedQuadRenderer implements VfxRenderer<WorldQuadData> {
    private final boolean glow;

    public TexturedQuadRenderer(boolean glow) {
        this.glow = glow;
    }

    @Override
    public void init() {
    }

    @Override
    public void render(VfxRenderContext ctx, List<? extends WorldQuadData> data) {
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

        var modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushMatrix();
        modelViewStack.set(viewMatrix);
        RenderSystem.applyModelViewMatrix();

        for (var quad : data) {
            var builder = Tesselator.getInstance().begin(
                    VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            submitCorner(builder, quad.corner0(), quad.uv0(), quad.color());
            submitCorner(builder, quad.corner1(), quad.uv1(), quad.color());
            submitCorner(builder, quad.corner2(), quad.uv2(), quad.color());
            submitCorner(builder, quad.corner3(), quad.uv3(), quad.color());
            MeshData meshData = builder.build();
            if (meshData == null) {
                continue;
            }
            RenderSystem.setShaderTexture(0, quad.texture());
            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            BufferUploader.drawWithShader(meshData);
        }

        modelViewStack.popMatrix();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.enableDepthTest();
    }

    private static void submitCorner(VertexConsumer consumer, Vector3f corner, Vector2f uv, Vector4f color) {
        consumer.addVertex(corner.x(), corner.y(), corner.z())
                .setUv(uv.x(), uv.y())
                .setColor(color.x(), color.y(), color.z(), color.w());
    }

    @Override
    public void close() {
    }
}
