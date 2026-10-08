package com.mojang.blaze3d.textures;

/**
 * 移植自 Minecraft 26.2 的 {@code com.mojang.blaze3d.textures.SamplerCache}.
 *
 * <p>26.2 以 {@code RenderSystem.getSamplerCache()} 获取采样器缓存, 按寻址/滤波创建共享采样器;
 * 1.21.1 以 {@code GlStateManager} 的采样状态表示, 无此类对象. 本垫层仅保符号 (C-stun),
 * 一律返回 {@link GpuSampler#DEFAULT}.
 */
public final class SamplerCache {
    /** 共享单例 (替代 {@code RenderSystem.getSamplerCache()}). */
    public static final SamplerCache INSTANCE = new SamplerCache();

    private SamplerCache() {
    }

    public GpuSampler getClampToEdge(FilterMode mode) {
        return GpuSampler.DEFAULT;
    }

    public GpuSampler getClampToEdge(FilterMode mode, boolean useMipmap) {
        return GpuSampler.DEFAULT;
    }
}
