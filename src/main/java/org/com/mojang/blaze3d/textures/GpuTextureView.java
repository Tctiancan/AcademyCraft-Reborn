package org.com.mojang.blaze3d.textures;

import net.minecraft.resources.ResourceLocation;

/**
 * 移植自 Minecraft 26.2 的 {@code com.mojang.blaze3d.textures.GpuTextureView}.
 *
 * <p>26.2 以 {@code GpuTextureView} 描述对 {@link GpuTexture} 的一处可采样视图.
 * 1.21.1 中纹理统一以 {@link ResourceLocation} 表示; 本垫层按仓库 C-stun 惯例保符号:
 * 视图背书一张纹理并透传其资源路径, 不持有实际 GPU 资源.
 */
public final class GpuTextureView {
    private final GpuTexture texture;

    public GpuTextureView(GpuTexture texture) {
        this.texture = texture;
    }

    /** 返回背书纹理. */
    public GpuTexture texture() {
        return texture;
    }

    /** 返回该视图对应的纹理资源路径. */
    public ResourceLocation location() {
        return texture == null ? null : texture.location();
    }

    /** 垫层中的"关闭"为空操作: 视图始终可用. */
    public void close() {
        // C-stun: 无 GPU 资源.
    }

    public boolean isClosed() {
        return false;
    }

    /** 由资源路径构造视图 (null 输入返回 null). */
    public static GpuTextureView of(ResourceLocation location) {
        return location == null ? null : new GpuTextureView(GpuTexture.of(location));
    }
}
