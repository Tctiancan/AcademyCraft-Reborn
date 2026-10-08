package org.academy.api.client.render.texture;

/**
 * 移植自 Minecraft 26.2 的 {@code com.mojang.blaze3d.textures.FilterMode}.
 *
 * <p>描述纹理采样滤波; 1.21.1 以 {@code GlStateManager} 的滤波状态等价表示, 该枚举仅保符号
 * (C-stun), 供既有形如 {@code FilterMode.LINEAR} 的创建采样器调用保持成立.
 */
public enum FilterMode {
    NEAREST,
    LINEAR;
}
