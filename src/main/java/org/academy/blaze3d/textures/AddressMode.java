package org.academy.blaze3d.textures;

/**
 * 移植自 Minecraft 26.2 的 {@code com.mojang.blaze3d.textures.AddressMode}.
 *
 * <p>描述纹理寻址 (包裹/夹取); 1.21.1 以 {@code GlStateManager.WrapMode} 表示. 该枚举仅保符号
 * (C-stun), 供形如 {@code AddressMode.REPEAT} 的创建采样器调用保持成立.
 */
public enum AddressMode {
    CLAMP_TO_EDGE,
    REPEAT,
    MIRRORED_REPEAT;
}
