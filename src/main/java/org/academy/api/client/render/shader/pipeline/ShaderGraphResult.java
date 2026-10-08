package org.academy.api.client.render.shader.pipeline;

import org.academy.api.client.render.RenderPipeline;

/**
 * 编译结果：构建好的管线 + 参数布局。
 *
 * <p>1.21.1 适配：26.2 携带的是具体的 {@code com.mojang.blaze3d.pipeline.RenderPipeline}；
 * 1.21.1 以项目自有 {@link org.academy.api.client.render.RenderPipeline} 抽象承载，
 * 供 {@code GpuDevice.precompilePipeline} 等 shim 消费。</p>
 */
public record ShaderGraphResult(RenderPipeline pipeline, UniformLayout layout) {
}
