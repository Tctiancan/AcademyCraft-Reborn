package org.academy.blaze3d.systems;

import org.academy.api.client.render.RenderPipeline;
import org.academy.api.client.render.shader.pipeline.CompiledRenderPipeline;
import org.academy.blaze3d.GpuFormat;
import org.academy.blaze3d.buffers.GpuBuffer;
import org.academy.blaze3d.shaders.ShaderSource;
import org.academy.blaze3d.textures.*;
import org.jspecify.annotations.Nullable;

import java.util.OptionalDouble;
import java.util.function.Supplier;

/**
 * 移植自 Minecraft 26.2 的 {@code com.mojang.blaze3d.systems.GpuDevice}.
 *
 * <p>26.2 以 {@code GpuDevice} 作为命令缓冲区/缓冲对象/着色器预编译的统一入口, 访问方式为
 * {@code RenderSystem.getDevice()}; 1.21.1 使用即时态渲染, 无该对象, 也不存在
 * {@code RenderSystem.getDevice()}. 本垫层按仓库 C-stun 惯例提供保符号实现: 命令编码与缓冲
 * 创建为空操作, 仅返回 CPU 侧占位, 预编译仅把源 {@link RenderPipeline} 包装为
 * {@link CompiledRenderPipeline}.
 */
public class GpuDevice {
    /** 单例 (1.21.1 下无真实设备, 以共享实例替代 {@code RenderSystem.getDevice()}). */
    private static final GpuDevice INSTANCE = new GpuDevice();

    public static GpuDevice instance() {
        return INSTANCE;
    }

    /** 创建命令编码器 (C-stun: 提交为空操作). */
    public CommandEncoder createCommandEncoder() {
        return new CommandEncoder();
    }

    /** 按大小创建 GPU 缓冲对象 (C-stun: CPU 侧占位). */
    public GpuBuffer createBuffer(Supplier<String> name, int usage, long size) {
        return GpuBuffer.create(name, usage, size);
    }

    /** 创建纹理采样器 (C-stun: 一律返回默认采样器). */
    public GpuSampler createSampler(AddressMode addressModeU, AddressMode addressModeV,
                                    FilterMode minificationFilter, FilterMode magnificationFilter,
                                    int maxAnisotropy, OptionalDouble mipmapBias) {
        return GpuSampler.DEFAULT;
    }

    /** 创建 GPU 纹理 (C-stun: 背书空资源路径的 CPU 占位). */
    public GpuTexture createTexture(Supplier<String> name, int usage, GpuFormat format,
                                    int width, int height, int depth, int samples) {
        return new GpuTexture(null);
    }

    /** 创建纹理视图 (C-stun: 透传背书纹理). */
    public GpuTextureView createTextureView(GpuTexture texture) {
        return new GpuTextureView(texture);
    }

    /**
     * 清空管线缓存. 1.21.1 无编译期管线缓存, 为空操作 (C-stun).
     */
    public void clearPipelineCache() {
        // C-stun: 空操作.
    }

    /**
     * 预编译着色器图管线; 1.21.1 无编译期战场对象, 故仅保留源管线.
     */
    public CompiledRenderPipeline precompilePipeline(RenderPipeline pipeline, ShaderSource source) {
        return new CompiledRenderPipeline(pipeline);
    }

    /** 1.21.1 无设备信息查询, 返回 {@code null} (C-stun). */
    public @Nullable DeviceInfo getDeviceInfo() {
        return null;
    }
}
