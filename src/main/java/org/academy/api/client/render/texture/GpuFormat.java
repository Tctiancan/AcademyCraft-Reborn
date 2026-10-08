package org.academy.api.client.render.texture;

/**
 * 移植自 Minecraft 26.2 的 {@code com.mojang.blaze3d.GpuFormat}.
 *
 * <p>26.2 以 {@code GpuFormat} 描述 GPU 纹素的字节格式; 1.21.1 的
 * {@link com.mojang.blaze3d.pipeline.RenderTarget} 统一以 RGBA8 无符号归一化
 * (映射 /sRGB) 分配颜色附件. 本垫层仅保留符号, 供既有形如
 * {@code new TextureTarget(..., GpuFormat.RGBA8_UNORM)} 的调用保持成立.
 */
public final class GpuFormat {
    /** RGBA8 无符号归一化 (1.21.1 帧缓冲颜色附件的实际格式). */
    public static final GpuFormat RGBA8_UNORM = new GpuFormat(0);

    /** RGBA16 半精度浮点 (1.21.1 渲染目标不直接支持, 垫层中仍可占位使用). */
    public static final GpuFormat RGBA16_FLOAT = new GpuFormat(1);

    private final int id;

    private GpuFormat(int id) {
        this.id = id;
    }

    public int id() {
        return id;
    }
}
