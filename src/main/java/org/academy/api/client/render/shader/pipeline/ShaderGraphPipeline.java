package org.academy.api.client.render.shader.pipeline;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.academy.api.client.render.RenderPipeline;
import org.academy.api.client.render.graph.compile.CompiledGraph;
import org.academy.api.client.render.graph.model.Graph;
import org.academy.api.client.render.shader.codegen.GlslGenerator;
import org.academy.api.client.render.shader.codegen.GlslNodeRegistry;
import org.academy.blaze3d.systems.GpuDevice;

/**
 * Shader 图管线：代码生成 → 动态着色器源注册 → RenderPipeline 构建 → 预编译。
 *
 * <p>1.21.1 适配：26.2 以图形 API 无关抽象 ({@code GpuDevice/RenderPipeline/BindGroupLayout})
 * 构建并预编译管线；1.21.1 采用即时态渲染，由 {@code Render.java/VfxPipelines.java} 确立的
 * 自有 {@link RenderPipeline} + `RenderType` 抽象承载。此处在 {@code buildPipeline} 改走该抽象
 * ({@code RenderType.gui()} 实化，沿用现有先例) 而非 26.2 的具体管线构造器，预编译经
 * {@link GpuDevice#precompilePipeline} 垫层包装 (C-stun)。</p>
 */
public final class ShaderGraphPipeline {
    private final GlslNodeRegistry generators;
    private final DynamicShaderSource shaderSource = new DynamicShaderSource();

    public ShaderGraphPipeline(GlslNodeRegistry generators) {
        this.generators = generators;
    }

    public ShaderGraphResult compile(Graph graph, CompiledGraph compiled) {
        var program = new GlslGenerator(generators).generate(graph, compiled);
        var vertexId = shaderSource.register(program.vertexSource());
        var fragmentId = shaderSource.register(program.fragmentSource());
        var samplers = GlslGenerator.samplePlan(graph);
        var layout = new UniformLayout(graph.parameters(), samplers);
        return new ShaderGraphResult(buildPipeline(fragmentId, vertexId, samplers.size()), layout);
    }

    /**
     * 预编译到 GPU 管线缓存；仅在有 GpuDevice 的环境（游戏/桌面编辑器）调用。
     */
    public CompiledRenderPipeline precompile(GpuDevice device, ShaderGraphResult result) {
        return device.precompilePipeline(result.pipeline(), shaderSource);
    }

    private static RenderPipeline buildPipeline(ResourceLocation fragment, ResourceLocation vertex, int samplerCount) {
        var location = "academy/pipeline/" + fragment.getPath();
        // 1.21.1 无 26.2 的 RenderPipeline.builder()/BindGroupLayout/ColorTargetState；
        // 改走自有 RenderPipeline 抽象，以 RenderType.gui() 实化，沿用 Render.java/VfxPipelines.java 先例。
        return new RenderPipeline(
                location,
                java.util.List.of(DefaultVertexFormat.POSITION),
                VertexFormat.Mode.QUADS,
                RenderType.gui()
        );
    }
}
