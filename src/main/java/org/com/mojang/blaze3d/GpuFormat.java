package org.com.mojang.blaze3d;

/**
 * 移植自 Minecraft 26.2 的 {@code com.mojang.blaze3d.GpuFormat}.
 *
 * <p>26.2 以 {@code GpuFormat} 描述 GPU 纹素的字节格式; 1.21.1 的
 * {@link com.mojang.blaze3d.pipeline.RenderTarget} 统一以 RGBA8 无符号归一化分配颜色附件.
 * 本垫层按仓库 C-stun 惯例仅保符号, 供桌面编辑器既有形如
 * {@code GpuFormat.RGBA8_UNORM} 的调用保持成立.
 */
public final class GpuFormat {
    /** RGBA8 无符号归一化. */
    public static final GpuFormat RGBA8_UNORM = new GpuFormat(0);
    /** RGBA16 半精度浮点. */
    public static final GpuFormat RGBA16_FLOAT = new GpuFormat(1);

    private final int id;

    private GpuFormat(int id) {
        this.id = id;
    }

    public int id() {
        return id;
    }
}
