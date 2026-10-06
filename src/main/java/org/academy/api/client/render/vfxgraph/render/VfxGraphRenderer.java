package org.academy.api.client.render.vfxgraph.render;

import java.util.List;
import java.util.function.Function;

import net.minecraft.resources.ResourceLocation;

import org.academy.api.client.render.vfxgraph.arc.ArcBuffer;
import org.academy.api.client.render.vfxgraph.sim.ParticleBuffer;
import org.jspecify.annotations.Nullable;

/**
 * C-stun: 26.2 GPU 延迟渲染管线实现（{@code GpuDevice}/{@code GpuTextureView}/{@code RenderPass} 等）
 * 在 1.21.1 中暂不移植。保留类结构与公共方法签名，实现为空占位。
 */
public final class VfxGraphRenderer {
    public record SurfaceMesh(float[] triangles, float r, float g, float b, float a) {
    }

    public VfxGraphRenderer() {
        this(null);
    }

    public VfxGraphRenderer(@Nullable Function<ResourceLocation, ?> textureLoader) {
        // C-stun: 26.2 GPU 资源初始化暂不移植。
    }

    public void setArcBuffer(@Nullable ArcBuffer buffer) {
        // C-stun: 世界 VFX 弧光渲染暂不移植。
    }

    public void render(ParticleBuffer buffer, GraphCamera camera, boolean clear) {
        // C-stun: 世界 VFX 渲染暂不移植。
    }

    public void render(ParticleBuffer buffer, GraphCamera camera, boolean clear, RenderSpec spec) {
        // C-stun: 世界 VFX 渲染暂不移植。
    }

    public void render(ParticleBuffer buffer, GraphCamera camera, boolean clear, List<RenderSpec> specs) {
        // C-stun: 世界 VFX 渲染暂不移植。
    }

    public void render(ParticleBuffer buffer, GraphCamera camera, boolean clear,
                       List<RenderSpec> specs, WorldTransform transform) {
        // C-stun: 世界 VFX 渲染暂不移植。
    }

    public void render(ParticleBuffer buffer, GraphCamera camera, boolean clear,
                       List<RenderSpec> specs, WorldTransform transform, boolean bloomPass) {
        // C-stun: 世界 VFX 渲染暂不移植。
    }

    public void render(ParticleBuffer buffer, GraphCamera camera, boolean clear,
                       List<RenderSpec> specs, WorldTransform transform, boolean bloomPass,
                       List<SurfaceMesh> surfaces) {
        // C-stun: 世界 VFX 渲染暂不移植。
    }

    public void drawArcTubes(ArcBuffer arcBuffer, GraphCamera camera, WorldTransform transform,
                             boolean bloomPass, RenderSpec spec) {
        // C-stun: 世界 VFX 弧光渲染暂不移植。
    }

    public void close() {
        // C-stun: 26.2 GPU 资源释放暂不移植。
    }
}
