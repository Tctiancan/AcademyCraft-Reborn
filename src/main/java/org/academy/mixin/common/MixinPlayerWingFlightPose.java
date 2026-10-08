package org.academy.mixin.common;

import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import org.academy.internal.common.ability.accelerator.flight.WingFlightPose;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class MixinPlayerWingFlightPose {
    @Inject(method = "updatePlayerPose", at = @At("HEAD"), cancellable = true)
    private void academy$useCompactWingFlightPose(CallbackInfo ci) {
        var player = (Player) (Object) this;
        if (player.getForcedPose() == null && WingFlightPose.usesCompactCollision(player)) {
            player.setPose(Pose.FALL_FLYING);
            ci.cancel();
        }
    }
}
