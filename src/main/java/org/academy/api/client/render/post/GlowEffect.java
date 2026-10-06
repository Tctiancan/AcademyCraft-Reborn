package org.academy.api.client.render.post;

import org.jspecify.annotations.Nullable;

/**
 * 1.21.1 简化版 (C-stun)。
 *
 * 26.2 通过命令编码器 + 多级高斯降采样完成辉光/bloom 后处理; 1.21.1 无对应 API,
 * 且该链路位于世界级 VFX。按首版简化策略整体跳过: 保留实例生命周期 (init/getInstance/
 * close) 与阶段访问 (getBefore/getAfter), process 为空操作, 不产生任何辉光合成。
 */
public final class GlowEffect {
    private static final Phase BEFORE = new Phase("before");
    private static final Phase AFTER = new Phase("after");
    @Nullable
    private static GlowEffect instance;

    private GlowEffect() {
    }

    public static void init() {
        instance = new GlowEffect();
    }

    public static GlowEffect getInstance() {
        if (instance == null) {
            throw new IllegalStateException("GlowEffect has not been initialized.");
        }
        return instance;
    }

    public static Phase getBefore() {
        return BEFORE;
    }

    public static Phase getAfter() {
        return AFTER;
    }

    public static void onResize() {
    }

    public void process() {
        // C-stun: 1.21.1 不执行辉光后处理。
    }

    public void close() {
        BEFORE.close();
        AFTER.close();
    }
}
