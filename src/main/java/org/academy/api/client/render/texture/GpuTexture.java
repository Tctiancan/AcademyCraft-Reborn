package org.academy.api.client.render.texture;

import net.minecraft.resources.ResourceLocation;

public final class GpuTexture {
    private final ResourceLocation location;

    public GpuTexture(ResourceLocation location) {
        this.location = location;
    }

    public ResourceLocation location() {
        return location;
    }

    public GpuTextureView getTextureView() {
        return GpuTextureView.of(location);
    }

    public static GpuTexture of(ResourceLocation location) {
        return location == null ? null : new GpuTexture(location);
    }
}
