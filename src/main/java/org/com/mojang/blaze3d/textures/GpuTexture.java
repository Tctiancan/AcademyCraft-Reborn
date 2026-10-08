package org.com.mojang.blaze3d.textures;

import net.minecraft.resources.ResourceLocation;

/**
 * 移植自 Minecraft 26.2 的 {@code com.mojang.blaze3d.textures.GpuTexture}.
 *
 * <p>26.2 以 {@code GpuTexture} 描述 GPU 纹理对象, 由 {@code GpuDevice.createTexture(...)} 创建,
 * 写作色/采样. 1.21.1 中纹理统一以 {@link ResourceLocation} 表示; 本垫层按仓库 C-stun 惯例保符号:
 * 一张 {@code GpuTexture} 背书一个资源路径, 不持有任何实际 GPU 资源.
 */
public final class GpuTexture {
    /** 用途位: 作渲染目标颜色附件 (C-stun 下仅为符号). */
    public static final int USAGE_RENDER_ATTACHMENT = 1;
    /** 用途位: 作采样纹理 (C-stun 下仅为符号). */
    public static final int USAGE_TEXTURE_BINDING = 2;
    /** 用途位: 作为复制目标 (C-stun 下仅为符号). */
    public static final int USAGE_COPY_DST = 4;

    private final ResourceLocation location;
    private final boolean closed;

    public GpuTexture(ResourceLocation location) {
        this.location = location;
        this.closed = false;
    }

    public ResourceLocation location() {
        return location;
    }

    /** 返回该纹理的视图. 1.21.1 下仅背书同一资源路径. */
    public GpuTextureView getTextureView() {
        return GpuTextureView.of(location);
    }

    /** 垫层中的"关闭"为空操作: 背书真实纹理, 始终可用. */
    public void close() {
        // C-stun: 无 GPU 显存.
    }

    public boolean isClosed() {
        return closed;
    }

    /** 由资源路径构造纹理 (null 输入返回 null). */
    public static GpuTexture of(ResourceLocation location) {
        return location == null ? null : new GpuTexture(location);
    }
}
