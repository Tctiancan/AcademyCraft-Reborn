package org.academy.api.client.render.vfxgraph.render;

import net.minecraft.resources.ResourceLocation;
import org.academy.api.client.render.vfxgraph.arc.ArcBuffer;
import org.academy.api.client.render.vfxgraph.arc.CurveToMeshBuilder;
import org.academy.api.client.render.vfxgraph.sim.ParticleBuffer;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.function.Function;

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

    /**
     * 电弧管顶点烘焙：把发射器局部坐标映射为相机相对世界坐标。
     *
     * <p>{@code final = worldTransform.apply(local * overallScale) - camPos}，法线经
     * {@code worldTransform.applyDirection} 变换后归一化。纯数据层，不依赖 GPU 后端。</p>
     */
    public static void transformArcTubeVertices(ByteBuffer buf, int vertexCount, Vector3f camPos,
                                                WorldTransform transform, float overallScale) {
        var world = new float[3];
        var dir = new float[3];
        for (int i = 0; i < vertexCount; i++) {
            int base = i * CurveToMeshBuilder.VERTEX_STRIDE;
            float x = buf.getFloat(base);
            float y = buf.getFloat(base + 4);
            float z = buf.getFloat(base + 8);
            transform.apply(x * overallScale, y * overallScale, z * overallScale, world);
            world[0] -= camPos.x;
            world[1] -= camPos.y;
            world[2] -= camPos.z;
            buf.putFloat(base, world[0]);
            buf.putFloat(base + 4, world[1]);
            buf.putFloat(base + 8, world[2]);

            float nx = buf.getFloat(base + 12);
            float ny = buf.getFloat(base + 16);
            float nz = buf.getFloat(base + 20);
            transform.applyDirection(nx * overallScale, ny * overallScale, nz * overallScale, dir);
            float len = (float) Math.sqrt(dir[0] * dir[0] + dir[1] * dir[1] + dir[2] * dir[2]);
            if (len > 1e-6f) {
                dir[0] /= len;
                dir[1] /= len;
                dir[2] /= len;
            }
            buf.putFloat(base + 12, dir[0]);
            buf.putFloat(base + 16, dir[1]);
            buf.putFloat(base + 20, dir[2]);
        }
    }

    /**
     * 场景深度是否可用于软粒子：仅 quad 几何且在无 Iris 着色器包（vanilla）时采样。
     */
    public static boolean sceneDepthUsable(RenderSpec.Geometry geometry, boolean irisShaderPackActive) {
        return geometry == RenderSpec.Geometry.QUAD && !irisShaderPackActive;
    }

    public void close() {
        // C-stun: 26.2 GPU 资源释放暂不移植。
    }
}
