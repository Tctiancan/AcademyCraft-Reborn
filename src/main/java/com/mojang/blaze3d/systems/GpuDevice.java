package com.mojang.blaze3d.systems;

import org.academy.api.client.render.RenderPipeline;
import org.academy.api.client.render.shader.pipeline.CompiledRenderPipeline;
import com.mojang.blaze3d.shaders.ShaderSource;
import org.jspecify.annotations.Nullable;

/**
 * 移植自 Minecraft 26.2 的 {@code com.mojang.blaze3d.systems.GpuDevice}.
 *
 * <p>26.2 以 {@code GpuDevice} 作为命令缓冲区/缓冲对象/着色器预编译的统一入口, 访问方式为
 * {@code RenderSystem.getDevice()}; 1.21.1 使用即时态渲染, 无该对象, 也不存在
 * {@code RenderSystem.getDevice()}. 本垫层按仓库 C-stun 惯例提供保符号实现: 命令编码与缓冲
 * 创建为空操作, 预编译仅把源 {@link RenderPipeline} 包装为 {@link CompiledRenderPipeline}.
 */
public class GpuDevice {
    /** 单例 (1.21.1 下无真实设备, 以共享实例替代 {@code RenderSystem.getDevice()}). */
    private static final GpuDevice INSTANCE = new GpuDevice();

    public static GpuDevice instance() {
        return INSTANCE;
    }

    /**
     * 预编译着色器图管线; 1.21.1 无编译期战场对象, 故仅保留源管线 (45C-legal).
     */
    public CompiledRenderPipeline precompilePipeline(RenderPipeline pipeline, ShaderSource source) {
        return new CompiledRenderPipeline(pipeline);
    }

    /** 1.21.1 无设备信息查询, 返回 {@code null} (C-stun). */
    public @Nullable DeviceInfo getDeviceInfo() {
        return null;
    }
}
