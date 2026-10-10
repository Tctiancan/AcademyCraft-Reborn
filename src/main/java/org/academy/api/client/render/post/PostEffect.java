package org.academy.api.client.render.post;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import org.jspecify.annotations.Nullable;

/**
 * 1.21.1 实装 (原 26.2 后处理离屏合成)。
 *
 * <p>26.2 经命令编码器 + {@code GpuTextureView} + blit pass 把世界场景复制到离屏目标,
 * 在其上叠加辉光等后处理, 再合成回主帧缓冲。1.21.1 无命令编码器, 但保留 {@link com.mojang.blaze3d.pipeline.RenderTarget}
 * 离屏帧缓冲与即时 (blit) 通道, 故此处以 1.21.1 原生方式实装同样的"捕获场景 → 离屏叠加 → 合成回屏"：
 *
 * <ul>
 *   <li>{@link #pre()} 先把主帧缓冲当前颜色内容复制进离屏目标 {@link #MAIN_SCENE} 作为底色,
 *       随后把写入目标切到 {@link #MAIN_SCENE} 使后置阶段 (辉光/粒子光晕) 在其上绘制;</li>
 *   <li>{@link #post()} 把写入目标切回主帧缓冲并把离屏结果合成回屏。</li>
 * </ul>
 *
 * 依赖 26.2 内部渲染状态 (方块区块层/实体线索流) 的 {@link WorldSurfaceMasks} 与
 * {@link DirectionalFieldRenderer} 仍属 C-stun: 1.21.1 无对应可移植的 GPU 延迟重建路径。
 */
public final class PostEffect {
    /** 主场景离屏目标: 保存世界底色, 后置阶段在其上叠加后合成回屏. 空表示尚未创建. */
    @Nullable
    public static RenderTarget MAIN_SCENE;
    public static final Phase PRE_PHASE = new Phase("pre");
    public static final Phase POST_PHASE = new Phase("post");

    private PostEffect() {
    }

    public static void resize(int width, int height) {
        if (MAIN_SCENE != null) {
            MAIN_SCENE.resize(width, height, Minecraft.ON_OSX);
        }
    }

    public static void pre() {
        if (MAIN_SCENE == null) {
            Window window = Minecraft.getInstance().getWindow();
            MAIN_SCENE = new TextureTarget(window.getWidth(), window.getHeight(), true, Minecraft.ON_OSX);
        }
        // 1) 捕获主帧缓冲当前颜色到离屏底色.
        int sourceColorId = Minecraft.getInstance().getMainRenderTarget().getColorTextureId();
        blitToTarget(sourceColorId, MAIN_SCENE);
        // 2) 后置阶段绘制切到离屏目标.
        MAIN_SCENE.bindWrite(true);
    }

    public static void post() {
        if (MAIN_SCENE == null) {
            return;
        }
        // 后置阶段 (扭曲环形/粒子光晕等) 叠加到离屏底色上.
        PRE_PHASE.draw();
        POST_PHASE.draw();
        MAIN_SCENE.unbindWrite();
        Window window = Minecraft.getInstance().getWindow();
        MAIN_SCENE.blitToScreen(window.getWidth(), window.getHeight());
    }

    public static Phase getPre() {
        return PRE_PHASE;
    }

    public static Phase getPost() {
        return POST_PHASE;
    }

    public static void close() {
        PRE_PHASE.close();
        POST_PHASE.close();
        if (MAIN_SCENE != null) {
            MAIN_SCENE.destroyBuffers();
            MAIN_SCENE = null;
        }
    }

    /**
     * 把一个颜色纹理源以全屏四边形采样绘制到目标帧缓冲 (相当于 26.2 的 blit pass).
     * 镜像 {@link RenderTarget} 内部 {@code _blitToScreen} 的即时状态切换, 但把写入目标
     * 置为给定 target 而非默认主帧缓冲。
     */
    private static void blitToTarget(int sourceColorTextureId, RenderTarget target) {
        RenderSystem.assertOnRenderThread();
        GlStateManager._colorMask(true, true, true, false);
        GlStateManager._depthMask(false);
        GlStateManager._disableDepthTest();
        GlStateManager._disableBlend();
        target.bindWrite(true);

        Minecraft minecraft = Minecraft.getInstance();
        ShaderInstance shaderinstance = Objects.requireNonNull(minecraft.gameRenderer.blitShader, "Blit shader not loaded");
        shaderinstance.setSampler("DiffuseSampler", sourceColorTextureId);
        shaderinstance.apply();

        BufferBuilder bufferbuilder = RenderSystem.renderThreadTesselator().begin(
                VertexFormat.Mode.QUADS, DefaultVertexFormat.BLIT_SCREEN);
        bufferbuilder.addVertex(0.0F, 0.0F, 0.0F);
        bufferbuilder.addVertex(1.0F, 0.0F, 0.0F);
        bufferbuilder.addVertex(1.0F, 1.0F, 0.0F);
        bufferbuilder.addVertex(0.0F, 1.0F, 0.0F);
        BufferUploader.draw(bufferbuilder.buildOrThrow());
        shaderinstance.clear();

        target.unbindWrite();
        GlStateManager._enableDepthTest();
        GlStateManager._depthMask(true);
        GlStateManager._colorMask(true, true, true, true);
    }
}
