package org.academy.internal.client.render.vfx;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import org.academy.api.client.render.vfx.VfxRenderContext;
import org.academy.api.client.render.vfx.VfxRenderer;
import org.joml.Matrix4f;

import java.util.List;

/**
 * 1.21.1 版本的方向打击地面特效渲染器。
 *
 * 26.2 通过 GpuDevice/命令编码器渲染; 1.21.1 使用推理式的即时渲染管线
 * ({@link Tesselator} + {@link BufferUploader#drawWithShader})。顶点数据由
 * {@link DirStrikeGroundData} 描述, 采用与 Minecraft 方块渲染一致的
 * {@link DefaultVertexFormat#BLOCK} 布局, 配合真实方块图集与光照贴图。
 */
public final class DirStrikeGroundRenderer implements VfxRenderer<DirStrikeGroundData> {
    @Override
    public void init() {
        // 1.21.1 即时渲染, 无 GPU 缓冲需要预分配。
    }

    @Override
    public void render(VfxRenderContext ctx, List<? extends DirStrikeGroundData> data) {
        if (data == null || data.isEmpty()) return;

        var cameraPos = ctx.cameraPos();
        var viewMatrix = new Matrix4f(ctx.viewRotationMatrix()).translate(
                (float) -cameraPos.x(), (float) -cameraPos.y(), (float) -cameraPos.z());

        var minecraft = Minecraft.getInstance();
        var modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushMatrix();
        modelViewStack.set(viewMatrix);
        RenderSystem.applyModelViewMatrix();

        RenderSystem.enableDepthTest();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
        minecraft.gameRenderer.lightTexture().turnOnLightLayer();

        for (var item : data) {
            drawLayer(item);
        }

        minecraft.gameRenderer.lightTexture().turnOffLightLayer();
        modelViewStack.popMatrix();
        RenderSystem.applyModelViewMatrix();
    }

    @Override
    public void close() {
        // 1.21.1 即时渲染, 无 GPU 资源需要释放。
    }

    private static void drawLayer(DirStrikeGroundData data) {
        var layer = data.layer();
        if (layer == RenderType.translucent()) {
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(
                    com.mojang.blaze3d.platform.GlStateManager.SourceFactor.SRC_ALPHA,
                    com.mojang.blaze3d.platform.GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                    com.mojang.blaze3d.platform.GlStateManager.SourceFactor.ONE,
                    com.mojang.blaze3d.platform.GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            RenderSystem.setShader(GameRenderer::getRendertypeTranslucentShader);
        } else if (layer == RenderType.cutout()) {
            RenderSystem.disableBlend();
            RenderSystem.setShader(GameRenderer::getRendertypeCutoutShader);
        } else {
            RenderSystem.disableBlend();
            RenderSystem.setShader(GameRenderer::getRendertypeSolidShader);
        }

        var buffer = data.vertices();
        buffer.rewind();
        var vertexCount = data.vertexCount();

        var builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
        for (var i = 0; i < vertexCount; i++) {
            var x = buffer.getFloat();
            var y = buffer.getFloat();
            var z = buffer.getFloat();
            var u = buffer.getFloat();
            var v = buffer.getFloat();
            var r = buffer.getFloat();
            var g = buffer.getFloat();
            var b = buffer.getFloat();
            var a = buffer.getFloat();
            var blockLight = buffer.getShort();
            var skyLight = buffer.getShort();
            var nx = buffer.get();
            var ny = buffer.get();
            var nz = buffer.get();
            buffer.get(); // 填充字节

            builder.addVertex(x, y, z)
                    .setColor((int) (r * 255.0f), (int) (g * 255.0f), (int) (b * 255.0f), (int) (a * 255.0f))
                    .setUv(u, v)
                    .setOverlay(0)
                    .setLight(packedLight(blockLight, skyLight))
                    .setNormal(unpackNormal(nx), unpackNormal(ny), unpackNormal(nz));
        }

        var meshData = builder.build();
        if (meshData != null) {
            BufferUploader.drawWithShader(meshData);
        }
    }

    private static int packedLight(short blockLight, short skyLight) {
        return ((skyLight & 0xFFFF) << 16) | (blockLight & 0xFFFF);
    }

    private static float unpackNormal(byte packed) {
        return packed / 127.0f;
    }
}
