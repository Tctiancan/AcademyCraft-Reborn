package org.academy.internal.client.render.vfx;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.academy.api.client.render.vfx.Vfx;
import org.academy.api.client.render.vfx.VfxFrameContext;
import org.academy.api.client.render.vfx.VfxSink;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class DirStrikeGroundEffect implements Vfx {
    private static final int MIN_Y_OFFSET = -3;
    private static final int MAX_Y_OFFSET = 5;
    private static final int MAX_GROUND_BLOCKS = 128;
    private static final int MAX_AIRBORNE_BLOCKS = 1024;
    private static final int MAX_ACTIVE_TIMELINES = 4;
    private static final int MAX_ACTIVE_BLOCKS = 2048;
    private static final int BASE_DURATION = 18;
    private static final int GROUND_HOLD_TICKS = 20;
    private static final int AIRBORNE_HOLD_TICKS = 60;
    private static final float BASE_PEAK = 0.38f;
    private static final float RISE_TICKS = 6.0f;
    private static final float DEG_TO_RAD = (float) (Mth.PI / 180.0);
    private static final float GROUND_SECTOR_COS = Mth.cos((float) (45.0 * Mth.DEG_TO_RAD));
    private static final int INITIAL_VERTICES = 65536;
    private static final List<Timeline> ACTIVE = new ArrayList<>();

    private ByteBuffer vertexData = BufferUtils.createByteBuffer(INITIAL_VERTICES * DirStrikeGroundData.VERTEX_STRIDE);

    public static void spawn(
            Vec3 center,
            BlockPos origin,
            int radius,
            boolean airborne,
            float lookX,
            float lookZ,
            long seed
    ) {
        var minecraft = Minecraft.getInstance();
        var level = minecraft.level;
        if (level == null || center == null || origin == null || radius <= 0) return;

        var modelRenderer = minecraft.getBlockRenderer().getModelRenderer();
        var samples = sampleSurface(
                level,
                center,
                origin,
                Math.min(radius, 32),
                airborne,
                normalizedLook(lookX, lookZ),
                seed,
                modelRenderer
        );
        if (samples.isEmpty()) return;

        var endTick = samples.stream().mapToDouble(BlockSample::timelineEndTick).max().orElse(1.0);
        var timeline = new Timeline(level, center, level.getGameTime(), samples, endTick);
        synchronized (ACTIVE) {
            while (!ACTIVE.isEmpty() && (ACTIVE.size() >= MAX_ACTIVE_TIMELINES
                    || activeBlockCount() + samples.size() > MAX_ACTIVE_BLOCKS)) {
                ACTIVE.removeFirst();
            }
            ACTIVE.add(timeline);
        }
        spawnDebris(level, samples, seed);
    }

    @Override
    public void sample(VfxFrameContext ctx, VfxSink sink) {
        var minecraft = Minecraft.getInstance();
        var level = minecraft.level;
        if (level == null) {
            clear();
            return;
        }

        var now = level.getGameTime() + ctx.partialTick();
        List<Timeline> timelines;
        synchronized (ACTIVE) {
            ACTIVE.removeIf(timeline -> timeline.level != level
                    || now - timeline.startTick > timeline.endTick + 2.0);
            if (ACTIVE.isEmpty()) return;
            timelines = List.copyOf(ACTIVE);
        }

        var requiredVertices = countVertices(timelines, now);
        if (requiredVertices == 0) return;
        ensureCapacity(requiredVertices);
        vertexData.clear();
        var start = vertexData.position();
        var vertexCount = renderVertices(vertexData, timelines, now);
        if (vertexCount == 0) return;
        var end = vertexData.position();
        var layerVertices = vertexData.duplicate();
        layerVertices.position(start).limit(end);
        sink.push(new DirStrikeGroundData(
                layerVertices.slice().order(vertexData.order()),
                vertexCount,
                RenderType.solid()
        ));
    }

    private void ensureCapacity(int requiredVertices) {
        var requiredBytes = (long) requiredVertices * DirStrikeGroundData.VERTEX_STRIDE;
        if (vertexData.capacity() >= requiredBytes) return;
        var newCapacity = Math.max(requiredVertices,
                (int) (vertexData.capacity() / DirStrikeGroundData.VERTEX_STRIDE) * 2);
        vertexData = BufferUtils.createByteBuffer(Math.toIntExact((long) newCapacity * DirStrikeGroundData.VERTEX_STRIDE));
    }

    private static int countVertices(List<Timeline> timelines, double now) {
        var count = 0;
        for (var timeline : timelines) {
            var age = now - timeline.startTick;
            for (var sample : timeline.samples) {
                var mesh = sample.mesh;
                if (mesh.isEmpty()) continue;
                var activeTick = (float) (age - sample.delay);
                if (activeTick < 0.0f || activeTick > sample.localEndTick()) continue;
                var amount = motion(activeTick, sample.duration, sample.holdTicks);
                if (amount <= 0.001f) continue;
                count += mesh.vertexCount();
            }
        }
        return count;
    }

    @Override
    public boolean isAlive() {
        return true;
    }

    static float motion(float activeTick, int duration, int holdTicks) {
        var riseTicks = Math.clamp(duration * 0.45f, 2.0f, RISE_TICKS);
        var fallTicks = Math.max(6.0f, duration - riseTicks);
        if (activeTick <= riseTicks) {
            var rise = Mth.clamp(activeTick / riseTicks, 0.0f, 1.0f);
            var inverse = 1.0f - rise;
            return 1.0f - inverse * inverse * inverse;
        }
        if (activeTick <= riseTicks + holdTicks) return 1.0f;
        var fall = Mth.clamp((activeTick - riseTicks - holdTicks) / fallTicks, 0.0f, 1.0f);
        return 1.0f - fall * fall;
    }

    private static int renderVertices(ByteBuffer vertexData, List<Timeline> timelines, double now) {
        var poseStack = new PoseStack();
        var rotation = new Quaternionf();
        var transformedPosition = new Vector3f();
        var vertexCount = 0;

        for (var timeline : timelines) {
            var age = now - timeline.startTick;
            for (var sample : timeline.samples) {
                var mesh = sample.mesh;
                var vertices = mesh.vertices;
                if (vertices.isEmpty()) continue;
                var activeTick = (float) (age - sample.delay);
                if (activeTick < 0.0f || activeTick > sample.localEndTick()) continue;
                var amount = motion(activeTick, sample.duration, sample.holdTicks);
                if (amount <= 0.001f) continue;

                poseStack.pushPose();
                poseStack.translate(
                        sample.position.getX() + 0.5,
                        sample.position.getY(),
                        sample.position.getZ() + 0.5
                );
                poseStack.translate(
                        0.0,
                        amount * sample.peakHeight * 1.12f + 0.03f + amount * sample.cornerLift,
                        0.0
                );
                poseStack.mulPose(rotation.rotationY(sample.yRotRadians));
                poseStack.translate(0.0, 0.0, amount * 0.1f);
                poseStack.mulPose(rotation.rotationX(-amount * 12.0f * DEG_TO_RAD));
                poseStack.mulPose(rotation.rotationZ(amount * 4.2f * DEG_TO_RAD));
                poseStack.mulPose(rotation.rotationX(amount * sample.cornerPitchRadians));
                poseStack.mulPose(rotation.rotationZ(amount * sample.cornerRollRadians));
                poseStack.scale(0.98f, 0.98f, 0.98f);
                poseStack.translate(-0.5, 0.0, -0.5);
                vertexCount += putVertices(poseStack.last(), vertexData, vertices, transformedPosition);
                poseStack.popPose();
            }
        }
        return vertexCount;
    }

    private static int putVertices(PoseStack.Pose pose, ByteBuffer target,
                                   List<CachedVertex> vertices, Vector3f position) {
        var positionMatrix = pose.pose();
        var normal = new Vector3f();
        var count = 0;
        for (var vertex : vertices) {
            positionMatrix.transformPosition(vertex.x, vertex.y, vertex.z, position);
            int packedColor = vertex.color;
            var r = (float) FastColor.ARGB32.red(packedColor) / 255.0f;
            var g = (float) FastColor.ARGB32.green(packedColor) / 255.0f;
            var b = (float) FastColor.ARGB32.blue(packedColor) / 255.0f;
            var a = (float) FastColor.ARGB32.alpha(packedColor) / 255.0f;
            target.putFloat(position.x);
            target.putFloat(position.y);
            target.putFloat(position.z);
            target.putFloat(vertex.u).putFloat(vertex.v);
            target.putFloat(r).putFloat(g).putFloat(b).putFloat(a);
            target.putShort(DirStrikeGroundData.packedBlockCoordinate(vertex.light));
            target.putShort(DirStrikeGroundData.packedSkyCoordinate(vertex.light));
            pose.transformNormal(vertex.normalX, vertex.normalY, vertex.normalZ, normal).normalize();
            target.put(DirStrikeGroundData.packNormal(normal.x));
            target.put(DirStrikeGroundData.packNormal(normal.y));
            target.put(DirStrikeGroundData.packNormal(normal.z));
            target.put((byte) 0);
            count++;
        }
        return count;
    }

    private static List<BlockSample> sampleSurface(
            ClientLevel level,
            Vec3 center,
            BlockPos origin,
            int radius,
            boolean airborne,
            Vec3 look,
            long seed,
            ModelBlockRenderer modelRenderer
    ) {
        var candidates = new ArrayList<BlockPos>();
        for (var xOffset = -radius; xOffset <= radius; xOffset++) {
            for (var zOffset = -radius; zOffset <= radius; zOffset++) {
                var distanceSqr = xOffset * xOffset + zOffset * zOffset;
                if (distanceSqr > radius * radius
                        || !airborne && !insideGroundSector(xOffset + 0.5, zOffset + 0.5, look)) {
                    continue;
                }
                var surface = findSurface(level, origin, xOffset, zOffset);
                if (surface != null) candidates.add(surface);
            }
        }

        var limit = airborne ? MAX_AIRBORNE_BLOCKS : MAX_GROUND_BLOCKS;
        if (candidates.size() > limit) {
            candidates.sort(Comparator.comparingLong(pos -> priority(seed, pos)));
            candidates.subList(limit, candidates.size()).clear();
        }
        candidates.sort(Comparator.comparingDouble(pos -> pos.distToCenterSqr(center)));

        var samples = new ArrayList<BlockSample>(candidates.size());
        for (var pos : candidates) {
            var state = level.getBlockState(pos);
            if (state.getRenderShape() != RenderShape.MODEL) continue;
            var mesh = bakeMesh(level, pos, state, modelRenderer);
            if (mesh.isEmpty()) continue;

            var random = RandomSource.create(seed ^ pos.asLong());
            var distance = Mth.sqrt((float) pos.distToCenterSqr(center));
            var delay = Math.max(0, Mth.floor(distance * 1.1f) - 1) + random.nextInt(2);
            var duration = BASE_DURATION + random.nextInt(3);
            var peak = BASE_PEAK + random.nextFloat() * 0.2f
                    + Math.max(0.0f, 1.0f - distance / Math.max(1, radius)) * 0.08f
                    - (airborne ? 0.2f : 0.0f);
            var outward = Vec3.atCenterOf(pos).subtract(center);
            if (outward.lengthSqr() <= 1.0E-4) outward = new Vec3(0.0, 0.0, 1.0);
            var modelSeed = state.getSeed(pos);
            samples.add(new BlockSample(
                    pos.immutable(),
                    state,
                    mesh,
                    delay,
                    duration,
                    airborne ? AIRBORNE_HOLD_TICKS : GROUND_HOLD_TICKS,
                    Math.max(0.05f, peak),
                    (float) Mth.atan2(outward.x, outward.z),
                    (((modelSeed >> 1) & 1L) == 0L ? 4.0f : -4.0f) * DEG_TO_RAD,
                    (((modelSeed >> 2) & 1L) == 0L ? 3.4f : -3.4f) * DEG_TO_RAD,
                    0.015f + ((modelSeed >> 3) & 3L) * 0.005f
            ));
        }
        return List.copyOf(samples);
    }

    private static CachedMesh bakeMesh(ClientLevel level, BlockPos pos, BlockState state,
                                       ModelBlockRenderer modelRenderer) {
        var minecraft = Minecraft.getInstance();
        var model = minecraft.getBlockRenderer().getBlockModel(state);
        var collector = new MeshCollector();
        try {
            modelRenderer.tesselateBlock(
                    level,
                    model,
                    state,
                    pos,
                    new PoseStack(),
                    collector,
                    false,
                    RandomSource.create(pos.asLong()),
                    pos.asLong(),
                    OverlayTexture.NO_OVERLAY,
                    ModelData.EMPTY,
                    null
            );
        } catch (Throwable throwable) {
            // 单个方块模型烘焙失败不应终止整场特效，跳过该方块。
        }
        return new CachedMesh(List.copyOf(collector.out));
    }

    private static BlockPos findSurface(ClientLevel level, BlockPos origin,
                                        int xOffset, int zOffset) {
        for (var yOffset = MAX_Y_OFFSET; yOffset >= MIN_Y_OFFSET; yOffset--) {
            var pos = origin.offset(xOffset, yOffset, zOffset);
            if (!level.hasChunkAt(pos)) continue;
            var state = level.getBlockState(pos);
            if (!renderableGround(level, pos, state)) continue;
            var above = pos.above();
            var aboveState = level.getBlockState(above);
            if (!aboveState.isAir() && !aboveState.getCollisionShape(level, above).isEmpty()) continue;
            return pos.immutable();
        }
        return null;
    }

    private static boolean renderableGround(ClientLevel level, BlockPos pos, BlockState state) {
        return !state.isAir()
                && !state.hasBlockEntity()
                && state.getRenderShape() == RenderShape.MODEL
                && state.getDestroySpeed(level, pos) >= 0.0f
                && state.getFluidState().isEmpty();
    }

    private static boolean insideGroundSector(double xOffset, double zOffset, Vec3 look) {
        var distanceSqr = xOffset * xOffset + zOffset * zOffset;
        if (distanceSqr <= 1.0E-8) return true;
        var inverseDistance = 1.0 / Mth.sqrt((float) distanceSqr);
        return (xOffset * look.x + zOffset * look.z) * inverseDistance >= GROUND_SECTOR_COS;
    }

    private static Vec3 normalizedLook(float lookX, float lookZ) {
        if (!Float.isFinite(lookX) || !Float.isFinite(lookZ)) return new Vec3(0.0, 0.0, 1.0);
        var look = new Vec3(lookX, 0.0, lookZ);
        return look.lengthSqr() <= 1.0E-8 ? new Vec3(0.0, 0.0, 1.0) : look.normalize();
    }

    private static long priority(long seed, BlockPos pos) {
        var value = seed ^ pos.asLong() * 0x9E3779B97F4A7C15L;
        value = (value ^ value >>> 30) * 0xBF58476D1CE4E5B9L;
        value = (value ^ value >>> 27) * 0x94D049BB133111EBL;
        return value ^ value >>> 31;
    }

    private static int activeBlockCount() {
        var count = 0;
        for (var timeline : ACTIVE) count += timeline.samples.size();
        return count;
    }

    private static void spawnDebris(ClientLevel level, List<BlockSample> samples, long seed) {
        var random = RandomSource.create(seed ^ 0xD1A57A1B10C5L);
        var stride = Math.max(2, samples.size() / 48);
        for (var index = 0; index < samples.size(); index += stride) {
            var sample = samples.get(index);
            var pos = sample.position;
            level.addParticle(
                    new BlockParticleOption(ParticleTypes.BLOCK, sample.state),
                    pos.getX() + 0.5,
                    pos.getY() + 0.9,
                    pos.getZ() + 0.5,
                    (random.nextDouble() - 0.5) * 0.08,
                    0.03 + random.nextDouble() * 0.05,
                    (random.nextDouble() - 0.5) * 0.08
            );
        }
    }

    private static void clear() {
        synchronized (ACTIVE) {
            ACTIVE.clear();
        }
    }

    private record Timeline(
            ClientLevel level,
            Vec3 center,
            long startTick,
            List<BlockSample> samples,
            double endTick
    ) {
    }

    private record BlockSample(
            BlockPos position,
            BlockState state,
            CachedMesh mesh,
            int delay,
            int duration,
            int holdTicks,
            float peakHeight,
            float yRotRadians,
            float cornerPitchRadians,
            float cornerRollRadians,
            float cornerLift
    ) {
        private float localEndTick() {
            return duration + holdTicks + 2.0f;
        }

        private float timelineEndTick() {
            return delay + localEndTick();
        }
    }

    private record CachedMesh(
            List<CachedVertex> vertices
    ) {
        private int vertexCount() {
            return vertices.size();
        }

        private boolean isEmpty() {
            return vertices.isEmpty();
        }
    }

    private record CachedVertex(
            float x,
            float y,
            float z,
            int color,
            float u,
            float v,
            int light,
            float normalX,
            float normalY,
            float normalZ
    ) {
    }

    /**
     * 1.21.1 的 {@link ModelBlockRenderer#tesselateBlock} 以 {@link VertexConsumer} 作为输出。
     * 该采集器覆写 {@link #addVertex(float,float,float,int,float,float,int,int,float,float,float)}
     * 直接把 GL 点数据收集为块本地坐标的 {@link CachedVertex} 列表。
     */
    private static final class MeshCollector implements VertexConsumer {
        private final List<CachedVertex> out = new ArrayList<>();

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            return this;
        }

        @Override
        public VertexConsumer setColor(int red, int green, int blue, int alpha) {
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer setNormal(float normalX, float normalY, float normalZ) {
            return this;
        }

        @Override
        public void addVertex(
                float x,
                float y,
                float z,
                int color,
                float u,
                float v,
                int packedOverlay,
                int packedLight,
                float normalX,
                float normalY,
                float normalZ
        ) {
            out.add(new CachedVertex(x, y, z, color, u, v, packedLight, normalX, normalY, normalZ));
        }
    }
}
