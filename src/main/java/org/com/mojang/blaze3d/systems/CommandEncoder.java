package org.com.mojang.blaze3d.systems;

import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.function.Supplier;

import org.com.mojang.blaze3d.textures.GpuTexture;
import org.com.mojang.blaze3d.textures.GpuTextureView;
import org.joml.Vector4f;

/**
 * 移植自 Minecraft 26.2 的 {@code com.mojang.blaze3d.systems.CommandEncoder}.
 *
 * <p>26.2 由 {@code GpuDevice.createCommandEncoder()} 获取命令编码器, 向 GPU 提交缓冲/纹理写入
 * 与渲染通道. 1.21.1 使用即时态渲染, 无该对象; 本垫层按仓库 C-stun 惯例保符号: 各类提交均
 * 为空操作, 仅守住既有调用形态.
 */
public final class CommandEncoder {
    CommandEncoder() {
    }

    public void clearColorTexture(GpuTextureView colorTexture, float clearColorRed,
                                  float clearColorGreen, float clearColorBlue, float clearColorAlpha) {
        // C-stun: 空操作.
    }

    public void clearColorTexture(GpuTextureView colorTexture, Vector4f clearColor) {
        // C-stun: 空操作.
    }

    public void writeToBuffer(ByteBuffer target, ByteBuffer data) {
        if (target != null && data != null) {
            int n = Math.min(target.remaining(), data.remaining());
            target.put(data.duplicate().limit(data.position() + n)).rewind();
            target.rewind();
            target.limit(n);
        }
    }

    public void writeToTexture(GpuTexture texture, ByteBuffer data, int x, int y, int z,
                               int width, int height) {
        // C-stun: 空操作.
    }

    public void writeToTexture(GpuTexture texture, ByteBuffer data, int x, int y, int z,
                               int width, int height, int mipLevel) {
        // C-stun: 空操作.
    }

    public RenderPass createRenderPass(Supplier<String> label, GpuTextureView colorTexture,
                                       Optional<Vector4f> clearColor) {
        return new RenderPass();
    }

    public void submit() {
        // C-stun: 空操作.
    }
}
