package org.academy.api.client.render.post;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import org.jspecify.annotations.Nullable;

/**
 * 1.21.1 移植版后处理阶段容器。
 *
 * <p>26.2 经命令编码器 + {@code GpuTextureView} + blit pass 把世界场景复制到离屏目标,
 * 在其上叠加辉光等后处理, 再合成回主帧缓冲。本 1.21.1 端口曾以
 * {@link com.mojang.blaze3d.pipeline.RenderTarget} 离屏帧缓冲实装同样管线, 但该离屏切换
 * (bindWrite/blitToScreen) 在 1.21.1 的 {@code renderLevel RETURN} 时机执行后会破坏随后
 * {@code renderItemInHand} 的第一人称手部渲染目标/深度, 导致手部与手持物品隐去。
 *
 * <p>为恢复手部显示, 现关闭离屏捕获与合成 ({@link #pre()} 与 {@link #post()} 为空操作,
 * {@link #MAIN_SCENE} 保持为空), 等价于 C-stun。相位层由具体渲染器经
 * {@link #getPre()}/{@link #getPost()} 取得并自行提交。
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
        // 26.2 的「捕获场景 → 离屏叠加 → 合成回屏」在 1.21.1 的 renderLevel RETURN 时机执行时,
        // bindWrite/blit 会破坏随后 renderItemInHand 的手部渲染目标/深度, 导致第一人称手部与手持
        // 物品隐去。为恢复手部显示, 关闭离屏捕获与切换: 后置阶段改为直接在主帧缓冲上绘制。
        if (MAIN_SCENE != null) {
            MAIN_SCENE.destroyBuffers();
            MAIN_SCENE = null;
        }
    }

    /**
     * 后置阶段入口：本 1.21.1 移植版本为恢复第一人称手部显示，关闭离屏合成并在帧末后置阶段
     * 也不做任何全局状态改动 (等价于 porting24 的 C-stun 空操作)。相位层 (Phase) 渲染可通过
     * {@link #getPre()}/{@link #getPost()} 在美术驱动的具体渲染器中自行提交。
     */
    public static void post() {
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
}
