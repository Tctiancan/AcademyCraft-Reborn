package org.academy.internal.client.animation;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.academy.internal.common.world.level.block.AbilityDeveloperSleep;
import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;

public final class AbilityDeveloperSleepClient {
    private static final double SLEEPING_HEIGHT = 0.6875;

    private AbilityDeveloperSleepClient() {
    }

    /**
     * 1.21.1 适配：
     * <ul>
     *   <li>26.2 通过 {@code renderState.setRenderData(ContextKey, PodPose)} 与
     *       {@code renderState.pose / bedOrientation / walkAnimation*} 表达睡眠姿态，
     *       依赖不存在的 PlayerRenderState/LivingEntityRenderState；1.21.1 无渲染状态，改为
     *       在 {@link #applyModelTransform} 内直接做模型变换。</li>
     *   <li>26.2 的 5 参 {@code setupRotations(PlayerRenderState,...)} 对应 1.21.1 的
     *       {@code PlayerRenderer.setupRotations(AbstractClientPlayer, PoseStack, F,F,F,F,F)}，
     *       此处以实体 + partialTick 计算姿态。</li>
     *   <li>{@code extract(Player, PlayerRenderState)} 与 {@code stabilizeCamera(Camera, CameraRenderState)}
     *       仅操作 26.2 渲染状态，无 1.21.1 等价物且无调用方，已移除。</li>
     * </ul>
     */
    public static void applyModelTransform(LivingEntity entity, PoseStack poseStack, float partialTick) {
        var podPose = sample(entity, partialTick);
        if (podPose == null || podPose.angleDegrees() <= 0.0f) return;

        var pivotX = podPose.mainPosition().getX() - podPose.sleepingPosition().getX();
        var pivotZ = podPose.mainPosition().getZ() - podPose.sleepingPosition().getZ();
        poseStack.translate(pivotX, 0.0, pivotZ);
        poseStack.mulPose(rotation(podPose));
        poseStack.translate(-pivotX, 0.0, -pivotZ);
    }

    @Nullable
    public static CameraAdjustment cameraAdjustment(Camera camera, float partialTick) {
        if (!(camera.getEntity() instanceof LivingEntity livingEntity)) return null;
        var podPose = sample(livingEntity, partialTick);
        if (podPose == null || podPose.angleDegrees() <= 0.0f) return null;

        var pivot = Vec3.atBottomCenterOf(podPose.mainPosition()).add(0.0, SLEEPING_HEIGHT, 0.0);
        var rotatedOffset = camera.getPosition().subtract(pivot).toVector3f();
        rotation(podPose).transform(rotatedOffset);
        return new CameraAdjustment(
                pivot.add(rotatedOffset.x, rotatedOffset.y, rotatedOffset.z),
                podPose.angleDegrees()
        );
    }

    @Nullable
    private static PodPose sample(LivingEntity sleeper, float partialTick) {
        if (!sleeper.isSleeping()) return null;
        var level = sleeper.level();
        if (level == null) return null;
        var sleepingPosition = sleeper.getSleepingPos().orElse(null);
        if (sleepingPosition == null) return null;

        var developer = AbilityDeveloperSleep.getDeveloperAt(level, sleepingPosition);
        if (developer == null) return null;
        var facing = developer.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        return new PodPose(
                developer.getBlockPos(),
                sleepingPosition,
                facing,
                developer.getPodRotationDegrees(partialTick)
        );
    }

    private static Quaternionf rotation(PodPose podPose) {
        Direction axis = podPose.facing().getCounterClockWise();
        return new Quaternionf().rotationAxis(
                podPose.angleDegrees() * ((float) Math.PI / 180.0f),
                axis.getStepX(),
                0.0f,
                axis.getStepZ()
        );
    }

    public record PodPose(
            BlockPos mainPosition,
            BlockPos sleepingPosition,
            Direction facing,
            float angleDegrees
    ) {
    }

    public record CameraAdjustment(Vec3 position, float pitchOffset) {
    }
}
