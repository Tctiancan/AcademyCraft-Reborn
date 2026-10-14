package org.academy.api.client.render.post;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

/**
 * 1.21.1 移植版后处理阶段容器。
 *
 * <p>26.2 经命令编码器 + {@code GpuTextureView} + blit pass 把世界场景复制到离屏目标,
 * 在其上叠加辉光等后处理, 再合成回主帧缓冲。本 1.21.1 端口曾以
 * {@link com.mojang.blaze3d.pipeline.RenderTarget} 离屏帧缓冲实装同样管线, 但此前把离屏目标
 * {@code bindWrite}/{@code blitToScreen} 直接挂在 {@code renderLevel RETURN} 时机会破坏随后
 * {@code renderItemInHand} 的第一人称手部渲染目标/深度, 导致手部与手持物品隐去。
 *
 * <p>扭曲光环 (动能冲击波) 需采样场景作径向偏移, 无法纯 C-stun。为恢复该特效又不再破坏手部渲染,
 * 本移植采用「离屏只读快照」：{@code pre()} 仅负责分配/保持 {@link #MAIN_SCENE} 离屏目标;
 * {@link #drawPre()} 须在 mixin 的模型视图/投影窗口内调用: 先以单位矩阵把当前主帧缓冲颜色 blit 入
 * {@code MAIN_SCENE} ({@code MAIN_SCENE} 仅作为纹理被读取, 完成后立即把主帧缓冲恢复为活动绘制目标,
 * 绝不让离屏目标在帧内残留为绑定写目标), 再由 {@code PRE_PHASE} (扭曲阶段) 以扭曲 shader 采样该
 * 快照合成回主帧缓冲。由于主帧缓冲始终是唯一写目标、其深度纹理从未被动过, 第一人称手部渲染不受影响。
 *
 * <p>依赖 26.2 内部渲染状态 (方块区块层/实体线索流) 的 {@link WorldSurfaceMasks} 与
 * {@link DirectionalFieldRenderer} 仍属 C-stun: 1.21.1 无对应可移植的 GPU 延迟重建路径。
 */
public final class PostEffect {
    /** 主场景离屏只读快照: 保存世界底色, 扭曲阶段采样它实现径向偏移。空表示尚未创建。 */
    @Nullable
    public static RenderTarget MAIN_SCENE;

    /** 扭曲/辉光等后处理阶段。 */
    public static final Phase PRE_PHASE = new Phase("pre", false, true);
    public static final Phase POST_PHASE = new Phase("post");

    private static final int GL_LINEAR = 9729;

    private PostEffect() {
    }

    public static void resize(int width, int height) {
        if (MAIN_SCENE != null) {
            MAIN_SCENE.resize(width, height, Minecraft.ON_OSX);
        }
    }

    /** 阶段前处理: 分配并保持离屏场景快照目标, 与主帧缓冲同尺寸同分辨率。 */
    public static void pre() {
        var mc = Minecraft.getInstance();
        var main = mc.getMainRenderTarget();
        if (MAIN_SCENE == null && main != null) {
            MAIN_SCENE = new TextureTarget(main.width, main.height, false, Minecraft.ON_OSX);
            MAIN_SCENE.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
            MAIN_SCENE.setFilterMode(GL_LINEAR);
        }
    }

    /**
     * 在 mixin 的模型视图/投影窗口内冲刷后置阶段: 先把当前主帧缓冲颜色捕获进离屏场景快照,
     * 再让扭曲阶段 (PRE_PHASE) 采样快照合成回主帧缓冲。
     */
    public static void drawPre() {
        captureScene();
        PRE_PHASE.draw();
    }

    /** 后置阶段入口: 本 1.21.1 移植版 POST_PHASE 未使用, 无操作。 */
    public static void post() {
    }

    public static Phase getPre() {
        return PRE_PHASE;
    }

    public static Phase getPost() {
        return POST_PHASE;
    }

    /** 供扭曲阶段采样主循环柱的离屏场景颜色纹理 id。 */
    public static int getSceneColorTextureId() {
        return MAIN_SCENE == null ? -1 : MAIN_SCENE.getColorTextureId();
    }

    /** 把当前主帧缓冲颜色捕获进离屏场景快照, 并在结束后把主帧缓冲恢复为活动绘制目标。 */
    private static void captureScene() {
        var mc = Minecraft.getInstance();
        var main = mc.getMainRenderTarget();
        if (MAIN_SCENE == null || main == null) return;

        MAIN_SCENE.bindWrite(true);
        RenderSystem.disableBlend();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.setShaderTexture(0, main.getColorTextureId());
        RenderSystem.setShader(GameRenderer::getPositionTexShader);

        var modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushMatrix();
        modelViewStack.set(new Matrix4f());
        RenderSystem.applyModelViewMatrix();
        var prevProjection = RenderSystem.getProjectionMatrix();
        RenderSystem.setProjectionMatrix(new Matrix4f(), VertexSorting.DISTANCE_TO_ORIGIN);
        try {
            // drawWithShader 会消费并令 ByteBufferBuilder.Result 失效, 故全屏四边形必须每帧就地
            // 构建上传 (数量仅 4 顶点, 开销可忽略), 绝不能跨帧缓存复用。
            var quad = buildFullscreenQuad();
            try {
                BufferUploader.drawWithShader(quad);
            } finally {
                quad.close();
            }
        } finally {
            RenderSystem.setProjectionMatrix(prevProjection, VertexSorting.DISTANCE_TO_ORIGIN);
            modelViewStack.popMatrix();
            RenderSystem.applyModelViewMatrix();
        }

        // 恢复主帧缓冲为活动绘制目标, 并还原混合状态, 避免影响随后手部渲染。
        main.bindWrite(true);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
    }

    /** 构建 NDC -1..1、POSITION_TEX 的全屏四边形网格 (覆盖整个屏幕的 blit 采样)。 */
    private static MeshData buildFullscreenQuad() {
        var allocator = new ByteBufferBuilder(512);
        var builder = new BufferBuilder(allocator, VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        builder.addVertex(-1.0F, -1.0F, 0.0F).setUv(0.0F, 0.0F);
        builder.addVertex(1.0F, -1.0F, 0.0F).setUv(1.0F, 0.0F);
        builder.addVertex(1.0F, 1.0F, 0.0F).setUv(1.0F, 1.0F);
        builder.addVertex(-1.0F, 1.0F, 0.0F).setUv(0.0F, 1.0F);
        return builder.build();
    }

    public static void close() {
        PRE_PHASE.close();
        POST_PHASE.close();
        if (MAIN_SCENE != null) {
            MAIN_SCENE.destroyBuffers();
            MAIN_SCENE = null;
        }
    }
}
