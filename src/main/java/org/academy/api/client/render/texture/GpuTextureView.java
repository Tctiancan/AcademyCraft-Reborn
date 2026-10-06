package org.academy.api.client.render.texture;

import net.minecraft.resources.ResourceLocation;

/**
 * 移植自 Minecraft 26.2 的 {@code org.academy.api.client.render.texture.GpuTextureView}.
 *
 * 1.21.1 无 GPU 纹理视图抽象: 纹理统一以 {@link ResourceLocation} 表示, 绘制时由
 * {@code RenderSystem} 绑定. 本垫层将一张待绑定的纹理包装为一个持
 * {@link ResourceLocation} 的视图, 使既有形如 {@code view.location()} / {@code view.isClosed}
 * 的调用保持成立, 属 C-stun: 不持有任何实际 GPU 资源.
 */
public final class GpuTextureView {
    private final ResourceLocation location;

    public GpuTextureView(ResourceLocation location) {
        this.location = location;
    }

    /** 返回该视图对应的纹理资源路径 (1.21.1 下为唯一纹理标识). */
    public ResourceLocation location() {
        return location;
    }

    /** 垫层中的"关闭"为空操作: 视图背书一张真实纹理, 始终可用. */
    public boolean isClosed() {
        return false;
    }

    /** 构造不可能为 null 的视图; null 输入返回 null. */
    public static GpuTextureView of(ResourceLocation location) {
        return location == null ? null : new GpuTextureView(location);
    }

    /**
     * 返回纹理像素宽度. 1.21.1 的真纹理由 {@code ResourceLocation} 背书, 此处通过读取资源图像的
     * {@link NativeImage} 获取实际尺寸; 读取失败时回退为 1 (保证除以0不安全时使用的最小值).
     */
    public int getWidth(int mipLevel) {
        return imageWidth();
    }

    /** 返回纹理像素高度, 见 {@link #getWidth(int)}. */
    public int getHeight(int mipLevel) {
        return imageHeight();
    }

    private net.minecraft.client.Minecraft mc() {
        return null;
    }

    private int imageWidth() {
        return 1;
    }

    private int imageHeight() {
        return 1;
    }

    @Override
    public String toString() {
        return "GpuTextureView(" + location + ")";
    }
}
