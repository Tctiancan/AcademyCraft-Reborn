package org.academy.blaze3d.textures;

/**
 * 移植自 Minecraft 26.2 的 {@code com.mojang.blaze3d.textures.GpuSampler}.
 *
 * <p>26.2 以 {@code GpuSampler} 描述纹理采样器 (寻址/滤波/LOD), 由
 * {@code GpuDevice.createSampler(...)} 创建; 1.21.1 以 {@code GlStateManager} 的采样状态表示.
 * 本垫层仅保符号 (C-stun): 凡是调用返回的采样器均以共享单例替代.
 */
public final class GpuSampler {
    /** 默认采样器 (1.21.1 下无真实采样器对象, 以共享实例替代). */
    public static final GpuSampler DEFAULT = new GpuSampler();

    private GpuSampler() {
    }

    @Override
    public String toString() {
        return "GpuSampler{stub}";
    }
}
