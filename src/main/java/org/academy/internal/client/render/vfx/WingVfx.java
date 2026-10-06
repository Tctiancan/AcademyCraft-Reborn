package org.academy.internal.client.render.vfx;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;
import org.academy.api.client.render.vfx.Vfx;
import org.academy.api.client.render.vfx.VfxFrameContext;
import org.academy.api.client.render.vfx.VfxSink;
import org.academy.api.common.ability.VortexAttackPattern;

import java.util.List;

/**
 * 1.21.1 简化版 (C-stun)。
 *
 * 26.2 通过 vfxgraph 运行时 (VfxGraphManager/ActiveEffect/VortexRenderBudget/Std140Builder) 采样翅膀网格、
 * 风暴漩涡与黑色翅膀攻击图形。1.21.1 无对应图形运行时,且该渲染位于世界级 VFX 链路上。
 * 按首版简化策略保留对外 API 契约 (用于数据包/桥梁的回调方法),清空全部 GPU/图形实现。
 */
public final class WingVfx implements Vfx {
    @Deprecated
    public enum WingKind {
        STORM, BLACK, WHITE, PLATINUM
    }

    /** 1.21.1 简化占位诊断;字段签名保持与 26.2 版一致。 */
    public record BlackDiagnostics(int visibleEffects, int reservedVertices, long reservedUploadBytes,
                                   long started, long duplicate, long coalesced, long expired) {
    }

    private WingVfx() {
    }

    public static long blackActivationEpoch(int entityId) {
        return -1;
    }

    public static BlackDiagnostics blackDiagnostics() {
        return new BlackDiagnostics(0, 0, 0L, 0L, 0L, 0L, 0L);
    }

    public static void enqueueSweep(WingKind kind, int entityId, boolean leftWing,
                                    float yawOffsetDeg, float pitchOffsetDeg) {
        // C-stun: 1.21.1 不处理翅膀挥动动画。
    }

    public static void syncBlackState(int entityId, long epoch, long sequenceFloor, boolean active) {
        // C-stun: 1.21.1 不同步黑色翅膀状态。
    }

    public static void enqueueBlackAttack(int entityId, VortexAttackPattern pattern, List<Vec3> targets,
                                         long epoch, long sequence, float initialProgress) {
        // C-stun: 1.21.1 不播放黑色翅膀攻击图形。
    }

    public static void enqueueBlackToWhiteTransition(int entityId) {
        // C-stun: 1.21.1 不处理黑→白翅膀过渡。
    }

    public static void clientTick() {
        // C-stun: 1.21.1 暂无翅膀动画需要 tick。
    }

    public static void clearSweeps() {
        // C-stun: 1.21.1 无翅膀渲染资源需要清理。
    }

    public static void submitFirstPerson(PoseStack poseStack, MultiBufferSource bufferSource,
                                         LocalPlayer player, int packedLight, float partialTick) {
        // C-stun: 1.21.1 第一人称翅膀渲染留空。
    }

    public static boolean renderFirstPersonWhenHudHidden() {
        return false;
    }

    @Override
    public void sample(VfxFrameContext ctx, VfxSink sink) {
        // C-stun: 1.21.1 不生成翅膀渲染数据。
    }

    @Override
    public boolean isAlive() {
        return true;
    }
}
