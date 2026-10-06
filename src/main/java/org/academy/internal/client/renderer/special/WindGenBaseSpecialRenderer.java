package org.academy.internal.client.renderer.special;

/**
 * C-stun: 26.2 {@code NoDataSpecialModelRenderer} 专属特殊物品渲染在 1.21.1 中暂不移植。
 * 该方块物品将以常规方块模型渲染，本类仅保留单例占位。
 */
public final class WindGenBaseSpecialRenderer {
    public static final WindGenBaseSpecialRenderer INSTANCE = new WindGenBaseSpecialRenderer();

    private WindGenBaseSpecialRenderer() {
    }
}
