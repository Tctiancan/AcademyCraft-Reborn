package org.academy.api.client.render.post;

import com.mojang.blaze3d.pipeline.RenderTarget;
import org.jspecify.annotations.Nullable;

/**
 * 1.21.1 简化版 (C-stun)。
 *
 * 26.2 通过命令编码器 + GpuTextureView + blit pass 将世界场景复制到离屏目标, 再叠加后处理;
 * 1.21.1 无对应 API (RenderSystem.getDevice / GpuFormat / runBlitPass 均不存在)。按首版简化
 * 策略整体跳过: 保留阶段访问 (getPre/getPost) 供粒子/实体渲染器使用, pre/post/resize 为空操作,
 * MAIN_SCENE 置空, 不产生任何离屏合成。
 */
public final class PostEffect {
    @Nullable
    public static final RenderTarget MAIN_SCENE = null;
    public static final Phase PRE_PHASE = new Phase("pre");
    public static final Phase POST_PHASE = new Phase("post");

    private PostEffect() {
    }

    public static void resize(int width, int height) {
        // C-stun: 1.21.1 不重建离屏目标。
    }

    public static void pre() {
        // C-stun: 1.21.1 不执行离屏合成。
    }

    public static void post() {
        // C-stun: 1.21.1 不执行后处理合成。
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
    }
}
