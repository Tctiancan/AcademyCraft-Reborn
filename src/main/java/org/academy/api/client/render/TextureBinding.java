package org.academy.api.client.render;

import net.minecraft.resources.ResourceLocation;
import org.academy.api.client.render.texture.GpuSampler;
import org.academy.api.client.render.texture.GpuTextureView;
import org.jetbrains.annotations.Nullable;

/**
 * 移植自 26.2 `TextureBinding` (持 GpuTextureView/GpuSampler).
 * 1.21.1 无 GpuTextureView/GpuSampler, 改为持可绑定的 [ResourceLocation] 与 shader 采样槽.
 * [location] 为 null 时表示该槽位无需绑定 (控件仅依赖顶点颜色等).
 */
public record TextureBinding(String name, @Nullable ResourceLocation location, int samplerSlot) {
    public static final int DEFAULT_SLOT = 0;

    public TextureBinding(String name, @Nullable ResourceLocation location) {
        this(name, location, DEFAULT_SLOT);
    }

    /** 兼容 26.2 语义的便捷构造: 从 [view] 提取其位置作为绑定目标. */
    public TextureBinding(String name, @Nullable GpuTextureView view, GpuSampler sampler) {
        this(name, view == null ? null : view.location(), DEFAULT_SLOT);
    }
}
