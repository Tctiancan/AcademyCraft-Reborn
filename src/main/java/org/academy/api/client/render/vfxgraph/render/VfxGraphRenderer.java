package org.academy.api.client.render.vfxgraph.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import org.academy.api.client.resources.R;
import org.academy.api.client.render.vfxgraph.arc.ArcBuffer;
import org.academy.api.client.render.vfxgraph.arc.CurveToMeshBuilder;
import org.academy.api.client.render.vfxgraph.sim.ParticleBuffer;
import org.academy.internal.client.render.AcademyShaders;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

/**
 * C-stun: 26.2 GPU 延迟渲染管线实现（{@code GpuDevice}/{@code GpuTextureView}/{@code RenderPass} 等）
 * 在 1.21.1 中暂不移植。保留类结构与公共方法签名，实现为空占位。
 */
public final class VfxGraphRenderer {
    /** 1.21.1 即时绘制抽离：仅还原 VectorBlast 使用的 quad 几何 (cloud_vortex shader) 采样纹理。 */
    private static final float[][] QUAD_CORNERS = {{0f, 0f}, {1f, 0f}, {1f, 1f}, {0f, 1f}};
    private static final int NOISE_SIZE = 256;

    private @Nullable DynamicTexture noiseTexture;
    private @Nullable DynamicTexture farTexture;
    private boolean samplerTexturesReady;

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

    /**
     * 1.21.1 即时绘制入口：以 cloud_vortex 实例化 shader 冲刷 quad 粒子 (VectorBlast 云涡)。
     *
     * <p><b>26.2 → 1.21.1 语义调整：</b>26.2 以「UV 属性 + 相机 UBO(View/Projection)」做实例化；
     * 1.21.1 无 GPU 实例化 API，改为在 CPU 端把每个粒子展开成 4 个共享同一组 Instance* 属性的
     * 顶点，经 vertex buffer(QUADS+顺序索引) 一次性提交。InstancePos 为世界坐标减相机位置，
     * 顶点由 shader 用 ModelViewMat(纯旋转视图) 定位到屏幕，与 MixinLevelRenderer RETURN 挂点约定一致。</p>
     */
    public void renderWorld(ParticleBuffer buffer, GraphCamera camera, WorldTransform transform,
                            List<RenderSpec> specs, boolean bloomPass) {
        var spec = pickScreenPass(specs, bloomPass);
        if (spec == null || buffer.count() == 0) return;
        ensureSamplerTextures();
        var shader = AcademyShaders.getVfxgraphCloudVortexShader();
        if (shader == null) return;

        int count = buffer.count();
        int vertexCount = count * 4;
        long totalBytes = (long) AcademyShaders.VFX_GRAPH_CLOUD_VORTEX.getVertexSize() * vertexCount;
        if (totalBytes <= 0 || totalBytes > Integer.MAX_VALUE - 64) return;

        ByteBufferBuilder.Result result = buildCloudVortexMesh(buffer, count, camera, transform, (int) totalBytes);
        if (result == null) return;
        var drawState = new MeshData.DrawState(
                AcademyShaders.VFX_GRAPH_CLOUD_VORTEX, vertexCount, vertexCount / 4 * 6,
                VertexFormat.Mode.QUADS, VertexFormat.IndexType.least(vertexCount));
        MeshData mesh = new MeshData(result, drawState);
        try {
            drawCloudVortex(mesh, spec, shader, camera);
        } finally {
            mesh.close();
        }
    }

    /** 选择本次绘制采纳的 quad 规格；glow bloom pass 只绘制加色混合日，逐帧相机 pass 只绘制透明。 */
    private static @Nullable RenderSpec pickScreenPass(List<RenderSpec> specs, boolean bloomPass) {
        if (specs == null) return null;
        for (var s : specs) {
            if (s.geometry() != RenderSpec.Geometry.QUAD) continue;
            if (s.fragmentShader() != null && !s.fragmentShader().equals(R.shaders.core.vfxgraph_cloud_vortex)) continue;
            boolean additive = s.blend() == RenderSpec.Blend.ADDITIVE || s.blend() == RenderSpec.Blend.GLOW;
            if (additive == bloomPass) return s;
        }
        return null;
    }

    private static @Nullable ByteBufferBuilder.Result buildCloudVortexMesh(
            ParticleBuffer buffer, int count, GraphCamera camera, WorldTransform transform, int totalBytes) {
        boolean identity = transform.isIdentity();
        var cam = camera.position();
        int vertexSize = AcademyShaders.VFX_GRAPH_CLOUD_VORTEX.getVertexSize();
        ByteBufferBuilder vertices = new ByteBufferBuilder(totalBytes);
        long ptr = vertices.reserve(totalBytes);
        var world = new float[3];
        int base = 0;
        for (int i = 0; i < count; i++) {
            float px = buffer.positionX(i), py = buffer.positionY(i), pz = buffer.positionZ(i);
            if (!identity) {
                transform.apply(px, py, pz, world);
                px = world[0]; py = world[1]; pz = world[2];
            }
            float ix = px - cam.x, iy = py - cam.y, iz = pz - cam.z;
            float vx = buffer.velocityX(i), vy = buffer.velocityY(i), vz = buffer.velocityZ(i);
            if (!identity) {
                transform.applyDirection(vx, vy, vz, world);
                vx = world[0]; vy = world[1]; vz = world[2];
            }
            float size = buffer.size(i);
            float cr = buffer.colorR(i), cg = buffer.colorG(i), cb = buffer.colorB(i), a = buffer.alpha(i);
            float rot = buffer.rotation(i);
            float seed = buffer.seed(i);
            float life = buffer.lifetime(i);
            float age = life > 0 ? Math.min(1f, buffer.age(i) / life) : 0f;
            for (int corner = 0; corner < 4; corner++) {
                long o = ptr + base;
                MemoryUtil.memPutFloat(o, QUAD_CORNERS[corner][0]);
                MemoryUtil.memPutFloat(o + 4, QUAD_CORNERS[corner][1]);
                MemoryUtil.memPutFloat(o + 8, 0f);
                MemoryUtil.memPutFloat(o + 12, ix);
                MemoryUtil.memPutFloat(o + 16, iy);
                MemoryUtil.memPutFloat(o + 20, iz);
                MemoryUtil.memPutFloat(o + 24, vx);
                MemoryUtil.memPutFloat(o + 28, vy);
                MemoryUtil.memPutFloat(o + 32, vz);
                MemoryUtil.memPutFloat(o + 36, size);
                MemoryUtil.memPutFloat(o + 40, cr);
                MemoryUtil.memPutFloat(o + 44, cg);
                MemoryUtil.memPutFloat(o + 48, cb);
                MemoryUtil.memPutFloat(o + 52, a);
                MemoryUtil.memPutFloat(o + 56, rot);
                MemoryUtil.memPutFloat(o + 60, seed);
                MemoryUtil.memPutFloat(o + 64, age);
                base += vertexSize;
            }
        }
        return vertices.build();
    }

    private void drawCloudVortex(MeshData mesh, RenderSpec spec, ShaderInstance shader, GraphCamera camera) {
        RenderSystem.setShader(() -> shader);
        RenderSystem.setShaderTexture(0, noiseTexture == null ? 0 : noiseTexture.getId()); // Sampler0: 可平铺噪波
        RenderSystem.setShaderTexture(1, farTexture == null ? 0 : farTexture.getId());      // Sampler1: 场景深度兜底(0.0)
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        boolean additive = spec.blend() == RenderSpec.Blend.ADDITIVE || spec.blend() == RenderSpec.Blend.GLOW;
        RenderSystem.enableBlend();
        if (additive) {
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        } else {
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        }
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();

        var stack = RenderSystem.getModelViewStack();
        stack.pushMatrix();
        stack.set(camera.viewRotation()); // 纯旋转视图矩阵，顶点已是相机相对坐标
        RenderSystem.applyModelViewMatrix();
        var prevProjection = new Matrix4f(RenderSystem.getProjectionMatrix());
        RenderSystem.setProjectionMatrix(camera.projection(), VertexSorting.DISTANCE_TO_ORIGIN);
        try {
            BufferUploader.drawWithShader(mesh);
        } finally {
            RenderSystem.setProjectionMatrix(prevProjection, VertexSorting.DISTANCE_TO_ORIGIN);
            stack.popMatrix();
            RenderSystem.applyModelViewMatrix();
            RenderSystem.enableCull();
            RenderSystem.enableDepthTest();
            RenderSystem.disableBlend();
        }
    }

    /**
     * 惰性构建并注册噪声采样（Sampler0）与远景深度兜底（Sampler1=纯黑 R 0.0）动态纹理，
     * 对应 26.2 NOISE_SIZE=256 的三倍噪波平铺与 1x1 R32_FLOAT(0) 的 far 深度纹理。
     */
    private void ensureSamplerTextures() {
        if (samplerTexturesReady) return;
        var manager = Minecraft.getInstance().getTextureManager();
        if (noiseTexture == null) {
            var image = new NativeImage(NOISE_SIZE, NOISE_SIZE, true);
            buildNoiseTile(image);
            noiseTexture = new DynamicTexture(image);
            manager.register("academy_vfx_noise", noiseTexture);
        }
        if (farTexture == null) {
            var farImage = new NativeImage(1, 1, true);
            farImage.setPixelRGBA(0, 0, 0xFF000000); // ABGR: alpha 255, RGB 全 0 → 采样深度 0.0
            farTexture = new DynamicTexture(farImage);
            manager.register("academy_vfx_far", farTexture);
        }
        samplerTexturesReady = true;
    }

    private static void buildNoiseTile(NativeImage image) {
        var rng = new Random(0xC0FFEEL);
        int size = NOISE_SIZE;
        var grid = new float[size + 1][size + 1];
        for (var row : grid) {
            for (int x = 0; x <= size; x++) row[x] = rng.nextFloat();
        }
        var tile = new float[size * size];
        float min = Float.MAX_VALUE;
        float max = -Float.MAX_VALUE;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float sum = 0f;
                float amp = 0.5f;
                for (int o = 0; o < 3; o++) {
                    int freq = 1 << o;
                    sum += amp * valueNoise(x * (float) freq, y * (float) freq, size, grid);
                    amp *= 0.5f;
                }
                tile[y * size + x] = sum;
                min = Math.min(min, sum);
                max = Math.max(max, sum);
            }
        }
        float range = Math.max(max - min, 1e-6f);
        for (int i = 0; i < tile.length; i++) {
            int b = (int) ((tile[i] - min) / range * 255f);
            image.setPixelRGBA(i % size, i / size, 0xFF000000 | (b << 16) | (b << 8) | b);
        }
    }

    private static float valueNoise(float x, float y, int size, float[][] grid) {
        int xi = Math.floorMod((int) Math.floor(x), size);
        int yi = Math.floorMod((int) Math.floor(y), size);
        float xf = x - (float) Math.floor(x);
        float yf = y - (float) Math.floor(y);
        int x1 = (xi + 1) % size;
        int y1 = (yi + 1) % size;
        float u = xf * xf * (3f - 2f * xf);
        float v = yf * yf * (3f - 2f * yf);
        return grid[yi][xi] + (grid[yi][x1] - grid[yi][xi]) * u
                + (grid[y1][xi] - grid[yi][xi]) * v
                + (grid[yi][xi] - grid[yi][x1] - grid[y1][xi] + grid[y1][x1]) * u * v;
    }

    public void close() {
        // C-stun: 26.2 GPU 资源释放暂不移植。
    }
}
