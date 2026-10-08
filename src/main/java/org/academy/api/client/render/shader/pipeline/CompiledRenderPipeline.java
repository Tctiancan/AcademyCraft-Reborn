package org.academy.api.client.render.shader.pipeline;

import org.academy.api.client.render.RenderPipeline;

/**
 * 移植自 Minecraft 26.2 的编译后渲染管线句柄 (预编译产物).
 *
 * <p>26.2 的 {@code GpuDevice.precompilePipeline} 返回一个 {@code CompiledRenderPipeline},
 * 封装烘焙好的 {@link RenderPipeline} 与绑定布局等预编译信息; 1.21.1 使用即时态渲染,
 * 无编译期战场对象. 本垫层仅持有源 {@link RenderPipeline}, 使既有调用保持成立, 属 C-stun.
 */
public final class CompiledRenderPipeline {
    private final RenderPipeline pipeline;

    public CompiledRenderPipeline(RenderPipeline pipeline) {
        this.pipeline = pipeline;
    }

    /** 返回该编译产物所封装的目标渲染管线. */
    public RenderPipeline pipeline() {
        return pipeline;
    }
}
