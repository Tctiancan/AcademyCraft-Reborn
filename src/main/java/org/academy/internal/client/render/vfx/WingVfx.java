package org.academy.internal.client.render.vfx;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.academy.api.client.render.graph.type.Value;
import org.academy.api.client.render.vfx.BoundedAnimationTimeline;
import org.academy.api.client.render.vfx.Vfx;
import org.academy.api.client.render.vfx.VfxFrameContext;
import org.academy.api.client.render.vfx.VfxSink;
import org.academy.api.client.render.vfxgraph.runtime.ActiveEffect;
import org.academy.api.client.render.vfxgraph.runtime.VfxGraphManager;
import org.academy.api.client.render.vfxgraph.runtime.VortexRenderBudget;
import org.academy.api.client.util.VertexUtil;
import org.academy.api.common.ability.VortexAttackPattern;
import org.academy.api.common.util.ImprovedNoise;
import org.academy.internal.common.attachment.AttachmentTypes;
import org.joml.FrustumIntersection;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

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
    private static final ResourceLocation BLACK_GRAPH = ResourceLocation.fromNamespaceAndPath("academy", "vfxgraph/black_wings");
    private static final Map<Integer, ActiveEffect> BLACK_GRAPHS = new HashMap<>();
    private static final Map<Integer, BlackPlayback> BLACK_ATTACKS = new HashMap<>();
    private static final Map<Integer, BlackVisual> BLACK_VISUALS = new HashMap<>();
    private static final VortexRenderBudget BLACK_BUDGET = new VortexRenderBudget();

    private static int blackVisibleCount;
    private static long blackClockNanos;
    private static double blackTime;

    private static final class BlackVisual {
        final Matrix4f relativeRoot = new Matrix4f();
        boolean hasRoot;
        float capturedYaw;
        int requestedTier = 1;
        double lastVisible;
    }

    private static final class BlackPlayback {
        final long epoch;
        final BoundedAnimationTimeline<BlackAttack> timeline;
        final double receivedAt;
        boolean active;
        boolean entitySeen;
        BlackPlayback(long epoch, long sequenceFloor, boolean active) {
            this.epoch = epoch;
            this.active = active;
            receivedAt = blackTime;
            timeline = new BoundedAnimationTimeline<>(sequenceFloor);
        }
    }

    private record BlackAttack(VortexAttackPattern pattern, List<Vec3> targets) { }

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
        var playback = BLACK_ATTACKS.get(entityId);
        return playback == null || !playback.active ? -1 : playback.epoch;
    }

    public static BlackDiagnostics blackDiagnostics() {
        long started = 0, duplicates = 0, coalesced = 0, expired = 0;
        for (var playback : BLACK_ATTACKS.values()) {
            started += playback.timeline.started(); duplicates += playback.timeline.duplicates();
            coalesced += playback.timeline.coalesced(); expired += playback.timeline.expired();
        }
        return new BlackDiagnostics(blackVisibleCount, BLACK_BUDGET.vertices(), BLACK_BUDGET.bytes(),
                started, duplicates, coalesced, expired);
    }

    private static double blackVisualTime() {
        long now = System.nanoTime();
        if (blackClockNanos != 0 && !Minecraft.getInstance().isPaused())
            blackTime += Math.max(0, (now - blackClockNanos) / 1e9);
        blackClockNanos = now;
        return blackTime;
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
        var minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return;
        if (animationLevel != minecraft.level) { clearSweeps(); animationLevel = minecraft.level; }
        blackVisualTime();
        var previous = BLACK_ATTACKS.get(entityId);
        if (previous != null && previous.epoch > epoch) return;
        if (previous != null && previous.epoch == epoch && (active || !previous.active)) return;
        if (BLACK_ATTACKS.size() >= 256 && previous == null) return;
        BLACK_ATTACKS.put(entityId, new BlackPlayback(epoch, sequenceFloor, active));
    }

    public static void enqueueBlackAttack(int entityId, VortexAttackPattern pattern, List<Vec3> targets,
                                         long epoch, long sequence, float initialProgress) {
        var minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return;
        if (animationLevel != minecraft.level) { clearSweeps(); animationLevel = minecraft.level; }
        double now = blackVisualTime();
        var playback = BLACK_ATTACKS.get(entityId);
        if (playback == null || playback.epoch < epoch) {
            syncBlackState(entityId, epoch, 0, true);
            playback = BLACK_ATTACKS.get(entityId);
        }
        if (playback == null || playback.epoch != epoch || !playback.active) return;
        if (targets.size() != (pattern == VortexAttackPattern.FOURFOLD_SLAM ? 4 : 1)) return;
        for (var target : targets) {
            if (!Double.isFinite(target.x) || !Double.isFinite(target.y) || !Double.isFinite(target.z)) return;
        }
        playback.timeline.offer(sequence, new BlackAttack(pattern, List.copyOf(targets)), now,
                pattern.durationTicks() / 20.0, initialProgress);
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
        double now = blackVisualTime();
        BLACK_ATTACKS.entrySet().removeIf(entry -> {
            var playback = entry.getValue();
            playback.timeline.advance(now);
            if (!(minecraft.level.getEntity(entry.getKey()) instanceof Player player))
                return playback.entitySeen || now - playback.receivedAt > 0.5;
            playback.entitySeen = true;
            if (!player.isAlive()) return true;
            return false;
        });
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
        BLACK_GRAPHS.values().forEach(ActiveEffect::stop);
        BLACK_GRAPHS.clear();
        BLACK_VISUALS.clear();
        BLACK_BUDGET.beginFrame();
        blackVisibleCount = 0;
        BLACK_ATTACKS.clear();
        blackClockNanos = 0;
        blackTime = 0;
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
        sampleBlackGraphs(ctx, roots);
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

    private static void sampleBlackGraphs(VfxFrameContext ctx, Map<Integer, Matrix4f> capturedRoots) {
        var minecraft = Minecraft.getInstance();
        var level = minecraft.level;
        var manager = VfxGraphManager.INSTANCE;
        if (level == null || !manager.isInitialized()) return;
        double now = blackVisualTime();
        var camera = ctx.camera().pos();
        // 26.2 的 VfxFrameContext.camera() 携带真实投影矩阵; 1.21.1 解析管线刻意把投影置
        // 单位阵 (绘制时由 RenderSystem 提供), 若直接用 ctx.camera().projectionMatrix() 构造
        // 视锥, 会在 1 格开外便把黑翼误剔除。采样发生在 renderLevel RETURN 早期, RenderSystem
        // 投影仍是本帧等级投影, 此处取其真实值构造剔除视锥与投影混叠距离。
        var projection = new Matrix4f(RenderSystem.getProjectionMatrix());
        var frustum = new FrustumIntersection(new Matrix4f(projection)
                .mul(ctx.camera().viewRotationMatrix()));
        var players = new ArrayList<Player>(level.players());
        players.sort(Comparator.comparingDouble(player -> player == minecraft.player ? -1e12
                : player.distanceToSqr(camera.x, camera.y, camera.z)
                - (BLACK_ATTACKS.containsKey(player.getId()) && BLACK_ATTACKS.get(player.getId()).timeline.hasEvents() ? 256 : 0)));
        BLACK_BUDGET.beginFrame((int) players.stream().filter(player -> isActive(player,
                org.academy.internal.client.render.vfx.WingKind.BLACK)
                || BLACK_TO_WHITE_TRANSITIONS.containsKey(player.getId())).count());
        blackVisibleCount = 0;
        BLACK_GRAPHS.values().forEach(effect -> effect.setFrameVisible(false));
        var retained = new HashSet<Integer>();
        for (var player : players) {
            var transition = transitionProjection(player.getId(), ctx.gameTime());
            boolean active = isActive(player, org.academy.internal.client.render.vfx.WingKind.BLACK);
            if ((!active && transition == null) || !player.isAlive()) continue;
            var pose = transition == null ? null : transition.blackWing();
            if (pose != null && !pose.visible()) continue;
            retained.add(player.getId());
            var visual = BLACK_VISUALS.computeIfAbsent(player.getId(), id -> new BlackVisual());
            var position = player.getPosition(ctx.partialTick());
            float yaw = Mth.rotLerp(ctx.partialTick(), player.yBodyRotO, player.yBodyRot);
            var captured = capturedRoots.get(player.getId());
            if (captured != null) {
                visual.relativeRoot.translation((float) (camera.x - position.x), (float) (camera.y - position.y),
                        (float) (camera.z - position.z)).mul(captured);
                visual.capturedYaw = yaw;
                visual.hasRoot = true;
            }
            var playback = active && transition == null ? BLACK_ATTACKS.get(player.getId()) : null;
            if (playback != null) playback.timeline.advance(now);
            var current = playback == null || !playback.active ? null : playback.timeline.currentValue();
            var pending = playback == null || !playback.active ? null : playback.timeline.pendingValue();
            boolean fourfold = current != null && current.pattern == VortexAttackPattern.FOURFOLD_SLAM
                    || pending != null && pending.pattern == VortexAttackPattern.FOURFOLD_SLAM;
            double radius = Math.max(blackBoundsRadius(current, position), blackBoundsRadius(pending, position));
            double distance = position.distanceTo(new Vec3(camera.x, camera.y, camera.z));
            if (distance - radius > 128 || !frustum.testSphere((float) (position.x - camera.x),
                    (float) (position.y - camera.y + 1.4), (float) (position.z - camera.z), (float) radius)) {
                continue;
            }
            double projectedDistance = distance * 1.428 / Math.max(0.1, Math.abs(projection.m11()));
            visual.requestedTier = player == minecraft.player ? 0
                    : VortexRenderBudget.preferred(projectedDistance, visual.requestedTier);
            int tier = BLACK_BUDGET.allocate(visual.requestedTier, fourfold);
            if (tier < 0) {
                continue;
            }
            var detail = VortexRenderBudget.DETAILS.get(tier);
            visual.lastVisible = now;
            blackVisibleCount++;
            var effect = BLACK_GRAPHS.get(player.getId());
            if (effect == null || effect.isStopped()) {
                effect = manager.spawn(BLACK_GRAPH, new Vector3f());
                // This caller already tested full sweep bounds and reserved the geometry cost.
                effect.setAlwaysVisible(true);
                BLACK_GRAPHS.put(player.getId(), effect);
            }
            effect.setFrameVisible(true);
            Matrix4f root;
            if (captured != null) root = new Matrix4f(captured);
            else if (visual.hasRoot && player != minecraft.player) {
                root = new Matrix4f().translation((float) (position.x - camera.x), (float) (position.y - camera.y),
                        (float) (position.z - camera.z)).rotateY(-(yaw - visual.capturedYaw) * Mth.DEG_TO_RAD).mul(visual.relativeRoot);
            } else {
                root = new Matrix4f().translation((float) (position.x - camera.x), (float) (position.y - camera.y) + 1.4071875f,
                        (float) (position.z - camera.z)).rotateY(-yaw * Mth.DEG_TO_RAD).rotateX(Mth.PI).scale(0.9375f);
            }
            var transform = root.translate(0f, 0.30f, 0.12f).rotateX(Mth.PI);
            effect.setPosition(transform.getTranslation(new Vector3f()).add(camera));
            effect.setRotation(transform.getUnnormalizedRotation(new Quaternionf()));
            effect.setScale(transform.getScale(new Vector3f()).x);
            var graph = effect.effect();
            boolean clipBody = player == minecraft.player && minecraft.options.getCameraType().isFirstPerson()
                    && minecraft.getCameraEntity() == player;
            graph.setLiveParam("first_person_body_clip", Value.of(clipBody ? 1f : 0f));
            if (clipBody) {
                var inverse = new Matrix4f(transform).invert();
                var eye = inverse.transformPosition(new Vector3f());
                var minimum = new Vector3f(Float.POSITIVE_INFINITY);
                var maximum = new Vector3f(Float.NEGATIVE_INFINITY);
                float halfWidth = player.getBbWidth() * 0.4f;
                // Stop below the eyes so the eye is never inside its own occluder.
                float top = (float) (camera.y - position.y) - 0.18f;
                for (int corner = 0; corner < 8; corner++) {
                    var point = new Vector3f((float) (position.x - camera.x) + ((corner & 1) == 0 ? -halfWidth : halfWidth),
                            (float) (position.y - camera.y) + ((corner & 2) == 0 ? 0f : top),
                            (float) (position.z - camera.z) + ((corner & 4) == 0 ? -halfWidth : halfWidth));
                    inverse.transformPosition(point);
                    minimum.min(point);
                    maximum.max(point);
                }
                graph.setLiveParam("body_clip_eye", Value.of(eye));
                graph.setLiveParam("body_clip_min", Value.of(minimum));
                graph.setLiveParam("body_clip_max", Value.of(maximum));
            }
            graph.setLiveParam("vortex_time", Value.of((float) now));
            graph.setLiveParam("vortex_filaments", Value.of((float) detail.filaments()));
            graph.setLiveParam("vortex_segments", Value.of((float) detail.segments()));
            graph.setLiveParam("vortex_rings", Value.of((float) detail.rings()));
            graph.setLiveParam("vortex_flecks", Value.of((float) detail.flecks()));
            graph.setLiveParam("radial_scale", Value.of(pose == null ? 1f : pose.radialScale()));
            graph.setLiveParam("length_scale", Value.of(pose == null ? 1f : pose.lengthScale()));
            graph.setLiveParam("spread_scale", Value.of(pose == null ? 1f : pose.spreadDegrees() / 30f));
            graph.setLiveParam("opacity", Value.of(1f));
            graph.setLiveParam("sweep_left", Value.of(0f)); graph.setLiveParam("sweep_right", Value.of(0f));
            graph.setLiveParam("pitch_left", Value.of(0f)); graph.setLiveParam("pitch_right", Value.of(0f));
            graph.setLiveParam("attack_mode", Value.of(0f)); graph.setLiveParam("attack_progress", Value.of(1f));
            var frame = playback == null || !playback.active ? null : playback.timeline.sample(now);
            if (frame == null) continue;
            var attack = frame.value();
            graph.setLiveParam("attack_mode", Value.of((float) attack.pattern.id()));
            graph.setLiveParam("attack_progress", Value.of(frame.progress()));
            var inverseRotation = new Quaternionf(effect.rotation()).conjugate();
            for (int i = 0; i < attack.targets.size(); i++) {
                var worldTarget = attack.targets.get(i);
                var target = new Vector3f((float) (worldTarget.x - effect.position().x),
                        (float) (worldTarget.y - effect.position().y), (float) (worldTarget.z - effect.position().z));
                inverseRotation.transform(target);
                target.div(Math.max(0.001f, effect.scale()));
                if (attack.pattern == VortexAttackPattern.FOURFOLD_SLAM) graph.setLiveParam("attack_corner_" + i, Value.of(target));
                else {
                    graph.setLiveParam("attack_target_x", Value.of(target.x));
                    graph.setLiveParam("attack_target_y", Value.of(target.y));
                    graph.setLiveParam("attack_target_z", Value.of(target.z));
                }
            }
        }
        BLACK_GRAPHS.entrySet().removeIf(entry -> {
            var visual = BLACK_VISUALS.get(entry.getKey());
            if (retained.contains(entry.getKey()) && visual != null && now - visual.lastVisible <= 0.75) return false;
            entry.getValue().stop();
            return true;
        });
        BLACK_VISUALS.keySet().removeIf(id -> !retained.contains(id));
    }

    private static double blackBoundsRadius(BlackAttack attack, Vec3 origin) {
        if (attack == null) return 10;
        double radius = 18;
        for (var target : attack.targets) radius = Math.max(radius, target.distanceTo(origin) + 16);
        return radius;
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
