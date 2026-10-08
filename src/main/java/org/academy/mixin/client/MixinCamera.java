package org.academy.mixin.client;

import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import org.academy.internal.client.animation.AbilityDeveloperSleepClient;
import org.academy.internal.client.ability.mentalout.CameraSmoothingBridge;
import org.academy.internal.client.ability.teleport.ChunkLeapGodView;
import org.academy.internal.client.ability.mentalout.PlayerControlClientState;
import org.academy.internal.client.ability.mentalout.WideAreaInterferenceClientState;
import org.academy.internal.client.render.vfx.CameraShakeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public abstract class MixinCamera implements CameraSmoothingBridge {
    @Shadow
    private float eyeHeight;
    @Shadow
    private float eyeHeightOld;

    private static float academy$cachedFovModifier;
    private static float academy$cachedOldFovModifier;

    @Invoker("setRotation")
    protected abstract void academy$setRotation(float yRot, float xRot);

    @Invoker("setPosition")
    protected abstract void academy$setPosition(Vec3 position);

    @Override
    public CameraSmoothingState academy$captureSmoothingState() {
        return new CameraSmoothingState(eyeHeight, eyeHeightOld, academy$cachedFovModifier, academy$cachedOldFovModifier);
    }

    @Override
    public void academy$restoreSmoothingState(CameraSmoothingState state) {
        eyeHeight = state.eyeHeight();
        eyeHeightOld = state.previousEyeHeight();
        academy$cachedFovModifier = state.fovModifier();
        academy$cachedOldFovModifier = state.previousFovModifier();
    }

    @Inject(method = "setup", at = @At("TAIL"))
    private void academy$applyControlledPlayerView(
            BlockGetter level, Entity entity, boolean detached, boolean thirdPersonReverse, float partialTick, CallbackInfo ci) {
        if (PlayerControlClientState.hasControllerView()) {
            academy$setRotation(
                    PlayerControlClientState.controllerViewYaw(),
                    PlayerControlClientState.controllerViewPitch()
            );
        }
    }

    @Inject(method = "setup", at = @At("TAIL"))
    private void academy$applySkyStrikeShake(
            BlockGetter level, Entity entity, boolean detached, boolean thirdPersonReverse, float partialTick, CallbackInfo ci) {
        var offset = CameraShakeManager.sample();
        if (offset.isZero()) return;
        var camera = (Camera) (Object) this;
        academy$setRotation(camera.getYRot() + offset.yaw(), camera.getXRot() + offset.pitch());
    }

    @Inject(method = "setup", at = @At("TAIL"))
    private void academy$alignWithAbilityDeveloperPod(
            BlockGetter level, Entity entity, boolean detached, boolean thirdPersonReverse, float partialTick, CallbackInfo ci) {
        var camera = (Camera) (Object) this;
        if (camera.getEntity() == null) return;
        var adjustment = AbilityDeveloperSleepClient.cameraAdjustment(
                camera,
                partialTick
        );
        if (adjustment == null) return;
        academy$setPosition(adjustment.position());
        academy$setRotation(camera.getYRot(), camera.getXRot() + adjustment.pitchOffset());
    }

    @Inject(method = "setup", at = @At("TAIL"))
    private void academy$applyWideAreaInterferenceCamera(
            BlockGetter level, Entity entity, boolean detached, boolean thirdPersonReverse, float partialTick, CallbackInfo ci) {
        if (!WideAreaInterferenceClientState.hasCameraOverride()) return;
        academy$setPosition(WideAreaInterferenceClientState.cameraPosition());
        academy$setRotation(
                WideAreaInterferenceClientState.cameraYaw(),
                WideAreaInterferenceClientState.cameraPitch()
        );
    }

    /**
     * Places the camera above the inspected location for the 区块跃迁 god view.
     *
     * <p>Same technique as the wide-area view: override the camera after vanilla has aligned it with the
     * player, so the real renderer draws the target while the player entity stays where it is.
     */
    @Inject(method = "setup", at = @At("TAIL"))
    private void academy$applyChunkLeapGodView(
            BlockGetter level, Entity entity, boolean detached, boolean thirdPersonReverse, float partialTick, CallbackInfo ci) {
        if (!ChunkLeapGodView.shouldOverrideCamera()) return;
        academy$setPosition(ChunkLeapGodView.cameraPosition());
        academy$setRotation(ChunkLeapGodView.cameraYaw(), ChunkLeapGodView.cameraPitch());
    }

    @Inject(method = "getMaxZoom", at = @At("HEAD"), cancellable = true)
    private void academy$ignoreDetachedViewCollision(
            float requestedDistance,
            CallbackInfoReturnable<Float> cir
    ) {
        if (WideAreaInterferenceClientState.hasCameraOverride()
                || ChunkLeapGodView.shouldOverrideCamera()) {
            cir.setReturnValue(requestedDistance);
        }
    }
}
