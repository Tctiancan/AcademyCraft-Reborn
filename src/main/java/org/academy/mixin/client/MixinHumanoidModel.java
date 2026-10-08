package org.academy.mixin.client;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.academy.internal.client.animation.GeckoPlayerModelAdapter;
import org.academy.internal.client.animation.WingFlightAnimationClient;
import org.academy.internal.client.animation.WingFlightAnimationTimeline;
import org.academy.internal.client.definitions.WingFlightAnimations;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Applies the authored full-body wing pose to player and matching armor models.
 */
@Mixin(HumanoidModel.class)
public abstract class MixinHumanoidModel {
    @Unique
    private ModelPart academy$root;
    @Unique
    private final Vector3f academy$animationVecCache = new Vector3f();

    @Inject(
            method = "<init>(Lnet/minecraft/client/model/geom/ModelPart;Ljava/util/function/Function;)V",
            at = @At("RETURN")
    )
    private void academy$captureRoot(ModelPart root, Function<ResourceLocation, RenderType> renderType, CallbackInfo ci) {
        this.academy$root = root;
    }

    @Inject(
            method = "setupAnim",
            at = @At("RETURN")
    )
    private void academy$applyWingFlightAnimation(
            LivingEntity entity,
            float limbSwing,
            float limbSwingAmount,
            float ageInTicks,
            float netHeadYaw,
            float headPitch,
            CallbackInfo ci
    ) {
        if (!(entity instanceof Player player)) return;
        var playback = WingFlightAnimationClient.playback(player.getId());
        if (playback == null || playback.phase() == WingFlightAnimationTimeline.Phase.IDLE) {
            org.academy.api.client.render.vfxgraph.shape.HumanoidSurfacePose.capture(player.getId(), (HumanoidModel<?>) (Object) this, academy$root);
            return;
        }

        var model = (HumanoidModel<?>) (Object) this;
        var mainArm = player.getMainArm();
        var vanillaMainArmPose = academy$getArm(model, mainArm).storePose();
        var vanillaOffhandArmPose = academy$getArm(model, mainArm.getOpposite()).storePose();
        var mainHandUse = player.isUsingItem() && player.getUsedItemHand() == InteractionHand.MAIN_HAND;
        var mainHandAttack = model.attackTime > 0.0f && player.swingingArm == InteractionHand.MAIN_HAND;
        var mainArmPose = mainArm == HumanoidArm.RIGHT
                ? model.rightArmPose
                : model.leftArmPose;

        academy$resetFlightParts(model);
        academy$animate(academy$root, academy$animation(model, playback.phase()),
                (long) (playback.clipTimeSeconds() * 1000.0f), 1.0f);
        GeckoPlayerModelAdapter.applyRootPivot(academy$root);

        // The Gecko clip is the body base pose. Player look and active hand actions
        // remain vanilla overlays, so flying does not lock the head or held item.
        model.head.xRot += headPitch * (float) (Math.PI / 180.0);
        model.head.yRot += netHeadYaw * (float) (Math.PI / 180.0);
        if (mainHandUse || mainHandAttack) {
            academy$getArm(model, mainArm).loadPose(vanillaMainArmPose);
            if (mainHandUse && mainArmPose.isTwoHanded()) {
                academy$getArm(model, mainArm.getOpposite()).loadPose(vanillaOffhandArmPose);
            }
        }
        org.academy.api.client.render.vfxgraph.shape.HumanoidSurfacePose.capture(player.getId(), model, academy$root);
    }

    @Unique
    private static ModelPart academy$getArm(HumanoidModel<?> model, HumanoidArm arm) {
        return arm == HumanoidArm.RIGHT ? model.rightArm : model.leftArm;
    }

    @Unique
    private void academy$resetFlightParts(HumanoidModel<?> model) {
        academy$root.resetPose();
        model.head.resetPose();
        model.hat.resetPose();
        model.body.resetPose();
        model.rightArm.resetPose();
        model.leftArm.resetPose();
        model.rightLeg.resetPose();
        model.leftLeg.resetPose();
    }

    @Unique
    private AnimationDefinition academy$animation(
            HumanoidModel<?> model,
            WingFlightAnimationTimeline.Phase phase
    ) {
        return switch (phase) {
            case START_FLYING_SLOW, STOP_FLYING_SLOW -> WingFlightAnimations.START_FLYING_SLOW;
            case FLYING_SLOW -> WingFlightAnimations.FLYING_SLOW;
            case START_FLYING_FAST -> WingFlightAnimations.START_FLYING_FAST;
            case FLYING_FAST -> WingFlightAnimations.FLYING_FAST;
            case QUIT_FLYING_FAST -> WingFlightAnimations.QUIT_FLYING_FAST;
            case IDLE -> throw new IllegalArgumentException("Idle wing animation has no clip");
        };
    }

    @Unique
    private void academy$animate(
            ModelPart root,
            AnimationDefinition definition,
            long accumulatedTime,
            float scale
    ) {
        float f = academy$getElapsedSeconds(definition, accumulatedTime);
        for (var entry : definition.boneAnimations().entrySet()) {
            String name = entry.getKey();
            List<AnimationChannel> list = entry.getValue();
            Optional<ModelPart> optional = academy$getAnyDescendantWithName(root, name);
            optional.ifPresent(part -> list.forEach(channel -> {
                Keyframe[] keyframes = channel.keyframes();
                int i = Math.max(0, Mth.binarySearch(0, keyframes.length, p -> f <= keyframes[p].timestamp()) - 1);
                int j = Math.min(keyframes.length - 1, i + 1);
                Keyframe keyframe0 = keyframes[i];
                Keyframe keyframe1 = keyframes[j];
                float f2;
                if (j != i) {
                    f2 = Mth.clamp((f - keyframe0.timestamp()) / (keyframe1.timestamp() - keyframe0.timestamp()), 0.0f, 1.0f);
                } else {
                    f2 = 0.0f;
                }
                keyframe1.interpolation().apply(academy$animationVecCache, f2, keyframes, i, j, scale);
                channel.target().apply(part, academy$animationVecCache);
            }));
        }
    }

    @Unique
    private static float academy$getElapsedSeconds(AnimationDefinition definition, long accumulatedTime) {
        float f = (float) accumulatedTime / 1000.0f;
        return definition.looping() ? f % definition.lengthInSeconds() : f;
    }

    @Unique
    private static Optional<ModelPart> academy$getAnyDescendantWithName(ModelPart root, String name) {
        if (root.hasChild(name)) return Optional.of(root.getChild(name));
        return root.getAllParts().filter(p -> p.hasChild(name)).findFirst().map(p -> p.getChild(name));
    }
}
