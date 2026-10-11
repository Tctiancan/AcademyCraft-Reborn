package org.academy.internal.client.render.vfx;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.academy.api.client.render.vfx.Vfx;
import org.academy.api.client.render.vfx.VfxFrameContext;
import org.academy.api.client.render.vfx.VfxSink;
import org.academy.api.client.util.VertexUtil;
import org.academy.api.common.ability.VortexAttackPattern;
import org.academy.api.common.util.ImprovedNoise;
import org.academy.internal.common.attachment.AttachmentTypes;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 1.21.1 实装 (原 26.2 翅膀 VFX)。
 *
 * <p>26.2 通过 vfxgraph 运行时与 Std140 实例矩阵缓冲提交翅膀环面几何; 1.21.1 无对应 GPU
 * 实例化/环面管线, 改为在 {@link #sample} 中把每个实例矩阵预先烘焙为世界坐标下的
 * POSITION_TEX 四边形流 (每顶点 20 字节), 交由 {@link WingRenderer} 以 positional_tex shader
 * 即时态冲刷。黑色翅膀攻击图形依赖 26.2 的 vfxgraph 资产运行时, 尚未移植, 保留占位。
 */
public final class WingVfx implements Vfx {
    private static final int SWEEP_DURATION_TICKS = 10;
    private static final float SWEEP_PIVOT_SIDE = 0.18f;
    private static final float SWEEP_PIVOT_FORWARD = 0.18f;
    private static final float SWEEP_PIVOT_UPPER_Z = -0.22f;
    private static final float SWEEP_BASE_YAW = 12.0f;
    private static final float SWEEP_ARC_DEGREES = 168.0f;
    private static final float SWEEP_SCALE = 1.12f;
    private static final float TORNADO_OFFSET_LEFT = 0.0f;
    private static final float TORNADO_OFFSET_RIGHT = 20.0f;

    static final int VERTEX_STRIDE = 5 * Float.BYTES;
    private static final int INITIAL_BUFFER_BYTES = 4096 * VERTEX_STRIDE;

    private static final int NUM_RINGS = 24;
    private static final float HEIGHT = 3.5f;
    private static final float SIZE = 1.0f;
    private static final float AVERAGE_GAP = HEIGHT / (float) (NUM_RINGS - 1);
    private static final float FUNNEL_BASE_RADIUS_FACTOR = 0.2F;
    private static final float FUNNEL_EXPONENT = 1.75F;
    private static final float HORIZONTAL_DISPLACEMENT_SCALE = 1.6f;
    private static final double POS_DOMAIN_WARP_SCALE = 0.15;
    private static final float GAP_VARIANCE_SCALE = 0.6f;
    private static final float BASE_RING_WIDTH = 0.075f * SIZE;
    private static final float RADIUS_BASE_NOISE_SCALE = 0.15f;
    private static final float RADIUS_EXTRA_NOISE_SCALE = 0.20f;
    private static final float RADIUS_JITTER_SCALE = 0.03f;
    private static final float ROTATION_BASE_NOISE_SCALE = 0.45f * Mth.PI;
    private static final float ROTATION_MODULATION_SCALE = 0.30f;
    private static final float RING_TILT_SCALE = 0.10f;
    private static final float WIDTH_BASE_NOISE_SCALE = 0.4f;
    private static final float WIDTH_DETAIL_NOISE_SCALE = 0.25f;
    private static final float TIME_SCALE_GLOBAL = 1.1f;
    private static final double TIME_POS_BASE = 0.07 * TIME_SCALE_GLOBAL;
    private static final double TIME_POS_WARP = 0.30 * TIME_SCALE_GLOBAL;
    private static final double TIME_GAP = 0.09 * TIME_SCALE_GLOBAL;
    private static final double TIME_RAD_BASE = 0.11 * TIME_SCALE_GLOBAL;
    private static final double TIME_RAD_EXTRA = 0.22 * TIME_SCALE_GLOBAL;
    private static final double TIME_JITTER = 0.85 * TIME_SCALE_GLOBAL;
    private static final double TIME_ROT_BASE = 0.55 * TIME_SCALE_GLOBAL;
    private static final double TIME_ROT_MOD = 0.16 * TIME_SCALE_GLOBAL;
    private static final double TIME_WIDTH_BASE = 0.15 * TIME_SCALE_GLOBAL;
    private static final double TIME_WIDTH_DETAIL = 0.55 * TIME_SCALE_GLOBAL;
    private static final double TIME_TILT = 0.18 * TIME_SCALE_GLOBAL;
    private static final float NESTED_RADIUS_FACTOR = 0.50f;
    private static final float NESTED_WIDTH_FACTOR = 0.75f;
    private static final float TORNADO_OFFSET_1 = 0.0f;
    private static final float TORNADO_OFFSET_2 = 20.0f;
    private static final float TORNADO_OFFSET_3 = 45.0f;
    private static final float TORNADO_OFFSET_4 = 70.0f;
    private static final int RINGS_PER_TORNADO = NUM_RINGS * 2;

    private static final Matrix4f WING_BASE_MATRIX = new Matrix4f()
            .rotateX(90.0f * Mth.DEG_TO_RAD)
            .translate(0, 0.25f, 0);
    private static final Matrix4f STORM_BASE_MATRIX = new Matrix4f()
            .rotateX(90.0f * Mth.DEG_TO_RAD)
            .translate(0, 0.25f, -0.25f);

    private static final int RING_SEGMENTS = 16;
    private static final float[][][] CACHED_VERTICAL_VERTEX_BUFFER =
            VertexUtil.Ring.getVerticalVertexBuffer(1.0f, 1.0f, RING_SEGMENTS);
    private static final int TORNADO_BYTES = RINGS_PER_TORNADO * RING_SEGMENTS * 4 * VERTEX_STRIDE;

    private static final Map<org.academy.internal.client.render.vfx.WingKind, SweepAnimationTimeline<SweepAnimation>> SWEEP_ANIMATIONS =
            createSweepTimelines();
    private static final Map<Integer, Long> BLACK_TO_WHITE_TRANSITIONS = new HashMap<>();
    private static ClientLevel animationLevel;

    private static EnumMap<org.academy.internal.client.render.vfx.WingKind, SweepAnimationTimeline<SweepAnimation>> createSweepTimelines() {
        var timelines = new EnumMap<org.academy.internal.client.render.vfx.WingKind, SweepAnimationTimeline<SweepAnimation>>(
                org.academy.internal.client.render.vfx.WingKind.class);
        for (var kind : org.academy.internal.client.render.vfx.WingKind.values()) {
            timelines.put(kind, new SweepAnimationTimeline<>());
        }
        return timelines;
    }

    /** 26.2 的 64 字节实例矩阵缓冲在 1.21.1 已改为按类型共享的内存几何缓冲, 借助 deprecated 枚举保持数据包/桥梁回调契约。 */
    @Deprecated
    public enum WingKind {
        STORM, BLACK, WHITE, PLATINUM
    }

    /** 守住 1.21.1 外部对 {@code WingVfx.WingKind} 的引用, 但在数据通道内始终使用包级 {@link org.academy.internal.client.render.vfx.WingKind}。 */
    private static org.academy.internal.client.render.vfx.WingKind dataKind(WingVfx.WingKind kind) {
        return switch (kind) {
            case STORM -> org.academy.internal.client.render.vfx.WingKind.STORM;
            case BLACK -> org.academy.internal.client.render.vfx.WingKind.BLACK;
            case WHITE -> org.academy.internal.client.render.vfx.WingKind.WHITE;
            case PLATINUM -> org.academy.internal.client.render.vfx.WingKind.PLATINUM;
        };
    }

    /** 1.21.1 简化占位诊断;字段签名保持与 26.2 版一致。 */
    public record BlackDiagnostics(int visibleEffects, int reservedVertices, long reservedUploadBytes,
                                   long started, long duplicate, long coalesced, long expired) {
    }

    private WingVfx() {
    }

    /** Shared instance used by {@link WingVfxClient}. */
    public static final WingVfx INSTANCE = new WingVfx();

    public static long blackActivationEpoch(int entityId) {
        return -1;
    }

    public static BlackDiagnostics blackDiagnostics() {
        return new BlackDiagnostics(0, 0, 0L, 0L, 0L, 0L, 0L);
    }

    public static void enqueueSweep(WingKind kind, int entityId, boolean leftWing,
                                    float yawOffsetDeg, float pitchOffsetDeg) {
        var minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return;
        if (animationLevel != minecraft.level) {
            clearSweeps();
            animationLevel = minecraft.level;
        }
        var entity = minecraft.level.getEntity(entityId);
        if (entity == null) return;
        SWEEP_ANIMATIONS.get(dataKind(kind)).enqueue(
                entityId,
                minecraft.level.getGameTime(),
                new SweepAnimation(leftWing, yawOffsetDeg, pitchOffsetDeg)
        );
    }

    public static void syncBlackState(int entityId, long epoch, long sequenceFloor, boolean active) {
        // C-stun: 1.21.1 黑色翅膀 vfxgraph 尚未移植, 不同步黑色翅膀状态。
    }

    public static void enqueueBlackAttack(int entityId, VortexAttackPattern pattern, List<Vec3> targets,
                                         long epoch, long sequence, float initialProgress) {
        // C-stun: 1.21.1 不播放黑色翅膀攻击图形。
    }

    public static void enqueueBlackToWhiteTransition(int entityId) {
        var minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.level.getEntity(entityId) == null) return;
        if (animationLevel != minecraft.level) {
            clearSweeps();
            animationLevel = minecraft.level;
        }
        BLACK_TO_WHITE_TRANSITIONS.put(entityId, minecraft.level.getGameTime());
    }

    public static void clientTick() {
        var minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            clearSweeps();
            return;
        }
        if (animationLevel != minecraft.level) {
            clearSweeps();
            animationLevel = minecraft.level;
            return;
        }
        var currentTick = minecraft.level.getGameTime();
        for (var timeline : SWEEP_ANIMATIONS.values()) {
            timeline.prune(currentTick, SWEEP_DURATION_TICKS,
                    entityId -> minecraft.level.getEntity(entityId) != null);
        }
        BLACK_TO_WHITE_TRANSITIONS.entrySet().removeIf(entry ->
                !WingTransitionAnimation.isActive(currentTick - entry.getValue())
                        || minecraft.level.getEntity(entry.getKey()) == null
        );
    }

    public static void clearSweeps() {
        for (var timeline : SWEEP_ANIMATIONS.values()) timeline.clear();
        BLACK_TO_WHITE_TRANSITIONS.clear();
        animationLevel = null;
    }

    public static void submitFirstPerson(PoseStack poseStack, MultiBufferSource bufferSource,
                                         LocalPlayer player, int packedLight, float partialTick) {
        // C-stun: 1.21.1 第一人称翅膀渲染由世界级 VFX 管线覆盖。
    }

    public static boolean renderFirstPersonWhenHudHidden() {
        var player = Minecraft.getInstance().player;
        return player == null
                || !player.getData(AttachmentTypes.ACTIVATED_PLATINUM_WING.get());
    }

    @Override
    public void sample(VfxFrameContext ctx, VfxSink sink) {
        var minecraft = Minecraft.getInstance();
        var level = minecraft.level;
        if (level == null) return;
        var roots = WingAvatarRegistry.entries();
        if (roots.isEmpty()) return;

        var partialTick = ctx.partialTick();
        var currentTick = (double) ctx.gameTime();
        var buffers = new EnumMap<org.academy.internal.client.render.vfx.WingKind, ByteBuffer>(
                org.academy.internal.client.render.vfx.WingKind.class);
        var ascensionBuffer = ensureBuffer(null);

        for (var root : roots.entrySet()) {
            var entity = level.getEntity(root.getKey());
            if (!(entity instanceof Player player)) continue;
            // 26.2: 解析/风暴翅膀只对已被 avatar capture 的玩家渲染, 无第一人称 fallback。
            // capture 取自 setupRotations 多态栈, 已含 玩家位置-相机 的相机相对平移;
            // 本地玩家第一人称身体不渲染→不 capture→风暴翅膀不渲染, 与 26.2 行为一致。
            var worldRoot = root.getValue();
            var transition = transitionProjection(player.getId(), currentTick);
            var effectTime = player.tickCount + partialTick;

            for (var kind : WingKind.values()) {
                if (kind == WingKind.BLACK) continue;
                var active = isActive(player, dataKind(kind));
                if (!active && !isTransitionWing(kind, transition)) continue;
                var buffer = ensureBuffer(buffers.get(dataKind(kind)));
                buffers.put(dataKind(kind), buffer);
                if (isTransitionWing(kind, transition)) {
                    var pose = kind == WingKind.BLACK ? transition.blackWing() : transition.whiteWing();
                    buffer = buildTransitionWings(buffer, worldRoot, dataKind(kind), effectTime, pose);
                } else {
                    buffer = buildPersistentWings(buffer, worldRoot, dataKind(kind), effectTime);
                }
                if (active) {
                    buffer = buildSweepAnimations(buffer, worldRoot, dataKind(kind), player.getId(),
                            currentTick, effectTime);
                }
                buffers.put(dataKind(kind), buffer);
            }

            if (transition != null && transition.ascension().visible()) {
                ascensionBuffer = buildTransitionWings(
                        ascensionBuffer,
                        worldRoot,
                        org.academy.internal.client.render.vfx.WingKind.WHITE,
                        effectTime,
                        transition.ascension()
                );
            }
        }

        for (var entry : buffers.entrySet()) {
            var buffer = entry.getValue().duplicate().order(ByteOrder.nativeOrder());
            buffer.flip();
            if (!buffer.hasRemaining()) continue;
            sink.push(new WingData(entry.getKey(), WingData.Layer.STABLE, copy(buffer)));
        }
        var asc = ascensionBuffer.duplicate().order(ByteOrder.nativeOrder());
        asc.flip();
        if (asc.hasRemaining()) {
            sink.push(new WingData(
                    org.academy.internal.client.render.vfx.WingKind.WHITE,
                    WingData.Layer.ASCENSION,
                    copy(asc)
            ));
        }
    }

    @Override
    public boolean isAlive() {
        return true;
    }

    private static boolean isActive(Player player, org.academy.internal.client.render.vfx.WingKind kind) {
        return switch (kind) {
            case STORM -> player.getData(AttachmentTypes.ACTIVATED_STORM_WING.get());
            case BLACK -> player.getData(AttachmentTypes.ACTIVATED_BLACK_WING.get());
            case WHITE -> player.getData(AttachmentTypes.ACTIVATED_WHITE_WING.get());
            case PLATINUM -> player.getData(AttachmentTypes.ACTIVATED_PLATINUM_WING.get());
        };
    }

    private static Matrix4f baseMatrix(org.academy.internal.client.render.vfx.WingKind kind) {
        return kind == org.academy.internal.client.render.vfx.WingKind.STORM ? STORM_BASE_MATRIX : WING_BASE_MATRIX;
    }

    private static float ringWidthScale(org.academy.internal.client.render.vfx.WingKind kind) {
        return switch (kind) {
            case STORM, BLACK -> 1.0f;
            case WHITE, PLATINUM -> 0.11f / 0.075f;
        };
    }

    private static int persistentTornadoCount(org.academy.internal.client.render.vfx.WingKind kind) {
        return kind == org.academy.internal.client.render.vfx.WingKind.STORM ? 4 : 2;
    }

    private static WingTransitionAnimation.Projection transitionProjection(int entityId, double currentTick) {
        var startTick = BLACK_TO_WHITE_TRANSITIONS.get(entityId);
        if (startTick == null) return null;
        var elapsedTicks = currentTick - startTick;
        return WingTransitionAnimation.isActive(elapsedTicks)
                ? WingTransitionAnimation.sample(elapsedTicks)
                : null;
    }

    private static boolean isTransitionWing(WingKind kind, WingTransitionAnimation.Projection transition) {
        return transition != null && (kind == WingKind.BLACK || kind == WingKind.WHITE);
    }

    private static ByteBuffer ensureBuffer(ByteBuffer buffer) {
        if (buffer == null) {
            return BufferUtils.createByteBuffer(INITIAL_BUFFER_BYTES);
        }
        return growBuffer(buffer, INITIAL_BUFFER_BYTES);
    }

    private static ByteBuffer growBuffer(ByteBuffer buffer, int extraBytes) {
        var needed = buffer.position() + extraBytes;
        if (buffer.capacity() >= needed) return buffer;
        var grown = BufferUtils.createByteBuffer(Math.max(needed, buffer.capacity() * 2));
        grown.put(buffer.duplicate().flip());
        return grown;
    }

    private static ByteBuffer copy(ByteBuffer src) {
        var out = BufferUtils.createByteBuffer(src.remaining()).order(ByteOrder.nativeOrder());
        out.put(src.duplicate());
        out.flip();
        return out;
    }

    private static ByteBuffer buildPersistentWings(ByteBuffer target, Matrix4f modelRoot,
                                             org.academy.internal.client.render.vfx.WingKind kind, float time) {
        var tornado = new Matrix4f(modelRoot).mul(baseMatrix(kind));
        var widthScale = ringWidthScale(kind);
        if (kind == org.academy.internal.client.render.vfx.WingKind.STORM) {
            var m1 = new Matrix4f(tornado).mul(new Matrix4f().rotate(new Quaternionf()
                    .rotateZ(30.0f * Mth.DEG_TO_RAD).rotateX(30.0f * Mth.DEG_TO_RAD)));
            target = bakeTornado(target, m1, time + TORNADO_OFFSET_1, widthScale);
            var m2 = new Matrix4f(tornado).mul(new Matrix4f().rotate(new Quaternionf()
                    .rotateZ(-30.0f * Mth.DEG_TO_RAD).rotateX(30.0f * Mth.DEG_TO_RAD)));
            target = bakeTornado(target, m2, time + TORNADO_OFFSET_2, widthScale);
            var m3 = new Matrix4f(tornado).mul(new Matrix4f().rotate(new Quaternionf()
                    .rotateZ(30.0f * Mth.DEG_TO_RAD).rotateX(-30.0f * Mth.DEG_TO_RAD)));
            target = bakeTornado(target, m3, time + TORNADO_OFFSET_3, widthScale);
            var m4 = new Matrix4f(tornado).mul(new Matrix4f().rotate(new Quaternionf()
                    .rotateZ(-30.0f * Mth.DEG_TO_RAD).rotateX(-30.0f * Mth.DEG_TO_RAD)));
            return bakeTornado(target, m4, time + TORNADO_OFFSET_4, widthScale);
        }
        return buildAdvancedWings(target, tornado, time, widthScale, 1.0f, 1.0f, 30.0f, 30.0f);
    }

    private static ByteBuffer buildTransitionWings(
            ByteBuffer target,
            Matrix4f modelRoot,
            org.academy.internal.client.render.vfx.WingKind kind,
            float time,
            WingTransitionAnimation.Pose pose
    ) {
        if (!pose.visible()) return target;
        var tornado = new Matrix4f(modelRoot).mul(WING_BASE_MATRIX);
        return buildAdvancedWings(
                target,
                tornado,
                time,
                ringWidthScale(kind),
                pose.radialScale(),
                pose.lengthScale(),
                pose.spreadDegrees(),
                pose.pitchDegrees()
        );
    }

    private static ByteBuffer buildAdvancedWings(
            ByteBuffer target,
            Matrix4f tornado,
            float time,
            float widthScale,
            float radialScale,
            float lengthScale,
            float spreadDegrees,
            float pitchDegrees
    ) {
        var left = new Matrix4f(tornado)
                .rotate(new Quaternionf()
                        .rotateZ(spreadDegrees * Mth.DEG_TO_RAD)
                        .rotateX(pitchDegrees * Mth.DEG_TO_RAD))
                .scale(radialScale, lengthScale, radialScale);
        target = bakeTornado(target, left, time + TORNADO_OFFSET_LEFT, widthScale);
        var right = new Matrix4f(tornado)
                .rotate(new Quaternionf()
                        .rotateZ(-spreadDegrees * Mth.DEG_TO_RAD)
                        .rotateX(pitchDegrees * Mth.DEG_TO_RAD))
                .scale(radialScale, lengthScale, radialScale);
        return bakeTornado(target, right, time + TORNADO_OFFSET_RIGHT, widthScale);
    }

    private static ByteBuffer buildSweepAnimations(ByteBuffer target, Matrix4f modelRoot,
                                             org.academy.internal.client.render.vfx.WingKind kind,
                                             int entityId, double currentTick, float effectTime) {
        if (entityId < 0) return target;
        var timeline = SWEEP_ANIMATIONS.get(kind);
        if (timeline == null) return target;
        var animations = timeline.entries(entityId);
        for (var entry : animations) {
            var progress = SweepAnimationTimeline.progress(entry, currentTick, SWEEP_DURATION_TICKS);
            if (progress < 0.0f || progress >= 1.0f) continue;
            var animation = entry.payload();
            var eased = 1.0f - (1.0f - progress) * (1.0f - progress);
            var side = animation.leftWing ? -1.0f : 1.0f;
            var sweepYaw = side * (SWEEP_BASE_YAW + SWEEP_ARC_DEGREES * eased) + animation.yawOffsetDeg;

            var matrix = new Matrix4f(modelRoot).mul(baseMatrix(kind));
            matrix.translate(side * SWEEP_PIVOT_SIDE, SWEEP_PIVOT_FORWARD, SWEEP_PIVOT_UPPER_Z);
            matrix.rotate(new Quaternionf().rotateZ(-sweepYaw * Mth.DEG_TO_RAD));
            matrix.rotate(new Quaternionf().rotateX(animation.pitchOffsetDeg * Mth.DEG_TO_RAD));
            matrix.rotate(new Quaternionf()
                    .rotateZ((animation.leftWing ? 30.0f : -30.0f) * Mth.DEG_TO_RAD)
                    .rotateX(30.0f * Mth.DEG_TO_RAD));
            matrix.scale(SWEEP_SCALE, SWEEP_SCALE, SWEEP_SCALE);
            target = bakeTornado(
                    target,
                    matrix,
                    effectTime + (animation.leftWing ? TORNADO_OFFSET_LEFT : TORNADO_OFFSET_RIGHT),
                    ringWidthScale(kind)
            );
        }
        return target;
    }

    private static ByteBuffer bakeTornado(ByteBuffer target, Matrix4f tornadoBase,
                                    float effectiveTime, float ringWidthScale) {
        target = growBuffer(target, TORNADO_BYTES);
        var tPosBase = effectiveTime * TIME_POS_BASE;
        var tWarp = effectiveTime * TIME_POS_WARP;
        var tGap = effectiveTime * TIME_GAP;
        var tRadBase = effectiveTime * TIME_RAD_BASE;
        var tRadExtra = effectiveTime * TIME_RAD_EXTRA;
        var tJitter = effectiveTime * TIME_JITTER;
        var tRotBase = effectiveTime * TIME_ROT_BASE;
        var tRotMod = effectiveTime * TIME_ROT_MOD;
        var tWidthBase = effectiveTime * TIME_WIDTH_BASE;
        var tWidthDetail = effectiveTime * TIME_WIDTH_DETAIL;
        var tTilt = effectiveTime * TIME_TILT;

        var currentY = 0.0f;
        var matrix = new Matrix4f();
        var tmpPos = new org.joml.Vector3f();
        var rotQuat = new Quaternionf();
        var tiltQuat = new Quaternionf();

        for (var i = 0; i < NUM_RINGS; i++) {
            var normalizedY = (NUM_RINGS <= 1) ? 0.5 : i / (double) (NUM_RINGS - 1);
            if (i > 0) currentY += calculateGap(i, tGap);

            var warpedY = normalizedY + ImprovedNoise.noise(normalizedY * 2.0, tWarp, 5.0) * POS_DOMAIN_WARP_SCALE;
            var actualY = currentY;

            var noiseX = ImprovedNoise.noise(warpedY * 1.3, tPosBase * 0.75, 10.0);
            var noiseZ = ImprovedNoise.noise(warpedY * 1.3, tPosBase * 0.75, 20.0);
            var heightScaleFactor = 0.4 + normalizedY * 1.6;
            var actualDx = (float) (noiseX * heightScaleFactor * SIZE * HORIZONTAL_DISPLACEMENT_SCALE * normalizedY);
            var actualDz = (float) (noiseZ * heightScaleFactor * SIZE * HORIZONTAL_DISPLACEMENT_SCALE * normalizedY);

            var rBase = calculateBaseRadius(normalizedY, tRadBase);
            var rWithExtra = addExtraRadiusNoise(rBase, i, tRadExtra);
            var rJitter = calculateRadiusJitter(i, tJitter);
            var finalRadiusMain = (float) Math.max(0.015 * SIZE, (rWithExtra + rJitter) * SIZE);

            var rotationAngle = calculateRotation(normalizedY, i, tRotBase, tRotMod);
            var ringWidth = calculateRingWidth(normalizedY, i, tWidthBase, tWidthDetail) * ringWidthScale;
            tiltQuat.identity()
                    .rotateZ((float) (ImprovedNoise.noise(i * 1.6, tTilt * 1.1, 90.0) * RING_TILT_SCALE))
                    .rotateX((float) (ImprovedNoise.noise(i * 1.6, tTilt * 1.1, 100.0) * RING_TILT_SCALE));
            rotQuat.identity().rotateY((float) rotationAngle).mul(tiltQuat);

            matrix.set(tornadoBase)
                    .translate(actualDx, actualY, actualDz)
                    .rotate(rotQuat)
                    .scale(finalRadiusMain, ringWidth, finalRadiusMain);
            bakeRing(target, matrix, tmpPos);

            var nestedBaseRadiusRaw = rWithExtra * NESTED_RADIUS_FACTOR;
            var nestedJitter = calculateRadiusJitter(i + NUM_RINGS, tJitter + 0.5);
            var finalRadiusNested = (float) Math.max(0.01 * SIZE, (nestedBaseRadiusRaw + nestedJitter) * SIZE);
            var nestedWidth = Math.max(0.01f * SIZE, ringWidth * NESTED_WIDTH_FACTOR);

            matrix.set(tornadoBase)
                    .translate(actualDx, actualY, actualDz)
                    .rotate(rotQuat)
                    .scale(finalRadiusNested, nestedWidth, finalRadiusNested);
            bakeRing(target, matrix, tmpPos);
        }
        return target;
    }

    private static void bakeRing(ByteBuffer target, Matrix4f matrix, org.joml.Vector3f tmpPos) {
        for (var i = 0; i < RING_SEGMENTS; i++) {
            var v0 = CACHED_VERTICAL_VERTEX_BUFFER[i][0];
            var v1 = CACHED_VERTICAL_VERTEX_BUFFER[i][1];
            var v2 = CACHED_VERTICAL_VERTEX_BUFFER[i][2];
            var v3 = CACHED_VERTICAL_VERTEX_BUFFER[i][3];
            putVertex(target, matrix, v0, 0.0f, tmpPos);
            putVertex(target, matrix, v1, 0.0f, tmpPos);
            putVertex(target, matrix, v2, 1.0f, tmpPos);
            putVertex(target, matrix, v3, 1.0f, tmpPos);
        }
    }

    private static void putVertex(ByteBuffer target, Matrix4f matrix, float[] local, float v,
                                  org.joml.Vector3f tmpPos) {
        matrix.transformPosition(local[0], local[1], local[2], tmpPos);
        target.putFloat(tmpPos.x);
        target.putFloat(tmpPos.y);
        target.putFloat(tmpPos.z);
        target.putFloat(local[3]);
        target.putFloat(v);
    }

    private static float calculateGap(int ringIndex, double timeGap) {
        var noise = (float) ImprovedNoise.noise(ringIndex * 0.7, timeGap, 11.0);
        var variablePart = noise * AVERAGE_GAP * GAP_VARIANCE_SCALE;
        return Math.max(AVERAGE_GAP * 0.2f, AVERAGE_GAP + variablePart);
    }

    private static double calculateBaseRadius(double normalizedY, double timeRadBase) {
        var funnelRadius = FUNNEL_BASE_RADIUS_FACTOR + Math.pow(normalizedY, FUNNEL_EXPONENT) * (1.0 - FUNNEL_BASE_RADIUS_FACTOR);
        var baseNoise = ImprovedNoise.noise(normalizedY * 1.1, timeRadBase * 0.8, 40.0);
        return funnelRadius * (1.0 + baseNoise * RADIUS_BASE_NOISE_SCALE);
    }

    private static double addExtraRadiusNoise(double baseRadius, int ringIndex, double timeRadExtra) {
        var extraNoise = ImprovedNoise.noise(ringIndex * 1.5, timeRadExtra * 1.5, 50.0);
        return baseRadius + extraNoise * RADIUS_EXTRA_NOISE_SCALE;
    }

    private static float calculateRadiusJitter(int ringIndex, double timeJitter) {
        return (float) (ImprovedNoise.noise(ringIndex * 3.0, timeJitter * 1.8, 55.0) * RADIUS_JITTER_SCALE);
    }

    private static double calculateRotation(double normalizedY, int ringIndex,
                                            double timeRotBase, double timeRotMod) {
        var modulation = (ImprovedNoise.noise(normalizedY * 0.7, timeRotMod, 65.0) + 1.0) * 0.5;
        var currentNoiseScale = ROTATION_BASE_NOISE_SCALE
                * (1.0 - ROTATION_MODULATION_SCALE + modulation * ROTATION_MODULATION_SCALE * 2.0);
        var baseRotation = timeRotBase * (1.0 + normalizedY * 0.35);
        var noiseOffset = ImprovedNoise.noise(ringIndex * 1.0, timeRotBase * 1.2, 60.0);
        return baseRotation + noiseOffset * currentNoiseScale;
    }

    private static float calculateRingWidth(double normalizedY, int ringIndex,
                                            double timeWidthBase, double timeWidthDetail) {
        var baseNoise = (float) ImprovedNoise.noise(normalizedY * 1.2, timeWidthBase, 80.0);
        var detailNoise = (float) ImprovedNoise.noise(ringIndex * 2.5, timeWidthDetail, 85.0);
        var width = BASE_RING_WIDTH
                * (1.0f + baseNoise * WIDTH_BASE_NOISE_SCALE + detailNoise * WIDTH_DETAIL_NOISE_SCALE);
        return Math.max(0.015f * SIZE, width);
    }

    private record SweepAnimation(boolean leftWing, float yawOffsetDeg, float pitchOffsetDeg) {
    }
}
