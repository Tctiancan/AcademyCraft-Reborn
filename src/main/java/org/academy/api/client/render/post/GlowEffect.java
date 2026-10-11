package org.academy.api.client.render.post;

import org.jspecify.annotations.Nullable;

/**
 * 1.21.1 实装 (原 26.2 辉光/bloom 后处理)。
 *
 * <p>26.2 通过命令编码器 + 多级高斯降采样完成辉光合成; 1.21.1 无对应 API, 且辉光辉染 shader 未移植。
 * 本实现以 {@link Phase} 承载辉光阶段 ({@link #getBefore()}/{@link #getAfter()}), {@link #process()}
 * 在离屏目标 {@code MAIN_SCENE} 之上以 {@code position_color} 冲刷阶段几何, 保留瞬时辉光形体而不含
 * 高斯 post 模糊。生命周期 (init/getInstance/close) 与原签名保持一致。
 */
public final class GlowEffect {
    private static final Phase BEFORE = new Phase("before", true);
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
        // 离屏目标 MAIN_SCENE 已由 PostEffect.pre() 绑为写入目标, 在此冲刷辉光阶段几何.
        BEFORE.draw();
        AFTER.draw();
    }

    public void close() {
        BEFORE.close();
        AFTER.close();
    }
}
