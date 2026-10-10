package org.academy.internal.client.render.vfx;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.GameRenderer;
import org.academy.api.client.render.vfx.VfxRenderContext;
import org.academy.api.client.render.vfx.VfxRenderer;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.List;

/**
 * 1.21.1 实装 (原 26.2 屏幕空间色闪)。
 *
 * <p>原 26.2 用独立 SCREEN_FLASH 管线绘制一张全屏 ndc 四边形并叠加 tint
 * {@code (0.80, 0.93, 1.0)}, 透明度取 {@code min(sum(alpha), cap)}。1.21.1 没有该管线,
 * 本实现把全屏 ndc 角点经当前视投影矩阵的逆反解回相机-相对世界坐标, 再复用世界级
 * {@code position_color} 即时冲刷路径绘制 (与 Beam/Lightning 相同的矩阵语义), 加法混合。
 */
public final class SkyStrikeScreenFlashRenderer implements VfxRenderer<SkyStrikeScreenFlashData> {
    private static final float COLOR_R = 0.80f;
    private static final float COLOR_G = 0.93f;
    private static final float COLOR_B = 1.0f;
    private static final float NDC_Z = 0.0f;

    @Override
    public void init() {
    }

    @Override
    public void render(VfxRenderContext ctx, List<? extends SkyStrikeScreenFlashData> data) {
        if (data.isEmpty()) {
            return;
        }
        var sum = 0.0f;
        var cap = 0.0f;
        for (var flash : data) {
            if (Float.isFinite(flash.alpha())) {
                sum += Math.max(0.0f, flash.alpha());
            }
            if (Float.isFinite(flash.cap())) {
                cap = Math.max(cap, flash.cap());
            }
        }
        var alpha = Math.min(sum, cap);
        if (alpha <= 0.001f) {
            return;
        }

        var view = new Matrix4f(ctx.viewRotationMatrix());
        var matrix = new Matrix4f(RenderSystem.getProjectionMatrix()).mul(view);
        var fullInv = new Matrix4f(matrix).invert();

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.disableDepthTest();

        var modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushMatrix();
        modelViewStack.set(view);
        RenderSystem.applyModelViewMatrix();

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        var builder = Tesselator.getInstance().begin(
                VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        emitCorner(builder, fullInv, -1.0f, -1.0f, alpha);
        emitCorner(builder, fullInv, 1.0f, -1.0f, alpha);
        emitCorner(builder, fullInv, 1.0f, 1.0f, alpha);
        emitCorner(builder, fullInv, -1.0f, -1.0f, alpha);
        emitCorner(builder, fullInv, 1.0f, 1.0f, alpha);
        emitCorner(builder, fullInv, -1.0f, 1.0f, alpha);
        MeshData meshData = builder.build();
        if (meshData != null) {
            BufferUploader.draw(meshData);
        }

        modelViewStack.popMatrix();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.enableDepthTest();
    }

    private static void emitCorner(VertexConsumer consumer, Matrix4f fullInv,
                                   float ndcX, float ndcY, float alpha) {
        var world = fullInv.transformPosition(new Vector3f(ndcX, ndcY, NDC_Z), new Vector3f());
        consumer.addVertex(world.x, world.y, world.z)
                .setColor(COLOR_R, COLOR_G, COLOR_B, alpha);
    }

    @Override
    public void close() {
    }
}
