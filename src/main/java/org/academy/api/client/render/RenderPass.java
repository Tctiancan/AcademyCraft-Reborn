package org.academy.api.client.render;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

import org.academy.api.client.render.texture.GpuSampler;
import org.academy.api.client.render.texture.GpuTextureView;
import org.com.mojang.blaze3d.PrimitiveTopology;

/**
 * 移植自 Minecraft 26.2 的 {@code com.mojang.blaze3d.systems.RenderPass}.
 *
 * <p>26.2 由 {@code CommandEncoder.createRenderPass(...)} 创建, 是向 GPU 提交一次渲染
 * (设置管线/Uniform/纹理绑定/索引缓冲等) 的封装, 以 {@link #use(Consumer)} 提交动作并自动收尾.
 * 1.21.1 使用即时态渲染, 无该对象; 本垫层按仓库 C-stun 惯例保符号: 各类设置均为空操作,
 * 仅守住既有调用形态.
 */
public final class RenderPass {
    RenderPass() {
    }

    /**
     * 以给定动作消费本渲染通道 (26.2 的 {@code renderPass.use { ... }} DSL). 1.21.1 下为空操作.
     */
    public void use(Consumer<RenderPass> consumer) {
        // C-stun: 1.21.1 无命令式渲染通道, 各设置均为空操作.
        if (consumer != null) {
            consumer.accept(this);
        }
    }

    public void setPipeline(RenderPipeline pipeline) {
        // C-stun: 空操作.
    }

    public void setUniform(String name, ByteBuffer data) {
        // C-stun: 空操作.
    }

    public void bindTexture(String name, GpuTextureView view, GpuSampler sampler) {
        // C-stun: 空操作.
    }

    public void setVertexBuffer(int slot, ByteBuffer buffer) {
        // C-stun: 空操作.
    }

    public void setIndexBuffer(ByteBuffer buffer, PrimitiveTopology type) {
        // C-stun: 空操作.
    }

    public void drawIndexed(int vertexCount, int instanceCount, int firstIndex, int baseVertex,
                            int firstInstance) {
        // C-stun: 空操作.
    }
}
