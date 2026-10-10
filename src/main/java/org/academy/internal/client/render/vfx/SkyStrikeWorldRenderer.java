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
import org.academy.api.client.render.vfx.VfxRenderContext;
import org.academy.api.client.render.vfx.VfxRenderer;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.List;

/**
 * 1.21.1 实装 (原 26.2 天空打击世界核心与辉光)。
 *
 * <p>26.2 通过 GpuDevice/命令编码器以实例化管线逐纹理批次延迟提交; 1.21.1 无对应 API, 改用即时态
 * 提交: 以相机-相对视旋转矩阵 (由 {@link VfxRenderContext#viewRotationMatrix()} 提供) 与
 * {@code RenderSystem} 当前投影矩阵合成模型视图, 把每只世界四方形以 {@code positional_tex_color}
 * shader 冲刷到当前帧缓冲。每四方形自带纹理与颜色; {@code glow} 为 true 时使用加法混合 (辉光),
 * 否则使用透明混合并保留深度测试 (核心柱体)。
 */
public final class SkyStrikeWorldRenderer implements VfxRenderer<SkyStrikeWorldData> {
    private static final Vector2f UV0 = new Vector2f(0.0f, 1.0f);
    private static final Vector2f UV1 = new Vector2f(1.0f, 1.0f);
    private static final Vector2f UV2 = new Vector2f(1.0f, 0.0f);
    private static final Vector2f UV3 = new Vector2f(0.0f, 0.0f);

    private final boolean glow;

    public SkyStrikeWorldRenderer(boolean glow) {
        this.glow = glow;
    }

    @Override
    public void init() {
    }

    @Override
    public void render(VfxRenderContext ctx, List<? extends SkyStrikeWorldData> data) {
        if (data.isEmpty()) {
            return;
        }
        var cameraPos = ctx.cameraPos();
        var viewMatrix = new Matrix4f(ctx.viewRotationMatrix())
                .translate(-cameraPos.x(), -cameraPos.y(), -cameraPos.z());

        RenderSystem.enableBlend();
        if (glow) {
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
            RenderSystem.disableDepthTest();
        } else {
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            RenderSystem.enableDepthTest();
        }

        var modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushMatrix();
        modelViewStack.set(viewMatrix);
        RenderSystem.applyModelViewMatrix();

        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        for (var quad : data) {
            var builder = Tesselator.getInstance().begin(
                    VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            submitCorner(builder, quad.corner0(), UV0, quad.color());
            submitCorner(builder, quad.corner1(), UV1, quad.color());
            submitCorner(builder, quad.corner2(), UV2, quad.color());
            submitCorner(builder, quad.corner3(), UV3, quad.color());
            MeshData meshData = builder.build();
            if (meshData == null) {
                continue;
            }
            RenderSystem.setShaderTexture(0, quad.texture());
            BufferUploader.draw(meshData);
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
