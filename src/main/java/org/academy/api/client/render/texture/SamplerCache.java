package org.academy.api.client.render.texture;

public final class SamplerCache {
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
