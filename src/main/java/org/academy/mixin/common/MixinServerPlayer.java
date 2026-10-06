package org.academy.mixin.common;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.academy.api.common.ability.ImagineBreakerHealthAccess;
import org.academy.api.server.damage.DefenseFeedbackSuppression;
import org.academy.internal.common.ability.accelerator.skills.lv4.VectorReflection;
import org.academy.internal.common.ability.darkmatter.targeting.DarkmatterTargeting;
import org.academy.internal.common.entitycontrol.EntityMotionGuard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(ServerPlayer.class)
public abstract class MixinServerPlayer extends Player implements ImagineBreakerHealthAccess {
    private MixinServerPlayer(Level level, GameProfile gameProfile) {
        super(level, gameProfile);
    }

    @Override
    public void imaginebreaker(float amount) {
        VectorReflection.Server.imaginebreaker((ServerPlayer) (Object) this, amount);
    }

    @Inject(method = "teleportTo(DDD)V", at = @At("HEAD"), cancellable = true)
    private void academy$guardSimplePlayerTeleport(double x, double y, double z, CallbackInfo ci) {
        if (EntityMotionGuard.shouldBlockTeleport((ServerPlayer) (Object) this)) ci.cancel();
    }

    @Inject(method = "teleportRelative", at = @At("HEAD"), cancellable = true)
    private void academy$guardRelativePlayerTeleport(double x, double y, double z, CallbackInfo ci) {
        if (EntityMotionGuard.shouldBlockTeleport((ServerPlayer) (Object) this)) ci.cancel();
    }

    @Inject(method = "moveTo(DDD)V", at = @At("HEAD"), cancellable = true)
    private void academy$guardPlayerPositionSnap(double x, double y, double z, CallbackInfo ci) {
        if (EntityMotionGuard.shouldBlockPositionSnap((ServerPlayer) (Object) this)) ci.cancel();
    }

    @Inject(
            method = "teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDLjava/util/Set;FF)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void academy$guardPlayerLevelTeleport(
            ServerLevel level,
            double x,
            double y,
            double z,
            Set<RelativeMovement> relatives,
            float yRot,
            float xRot,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (EntityMotionGuard.shouldBlockTeleport((ServerPlayer) (Object) this)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "indicateDamage(DD)V", at = @At("HEAD"), cancellable = true)
    private void academy$suppressDefenseHurtShake(double xd, double zd, CallbackInfo ci) {
        if (DefenseFeedbackSuppression.isSuppressed((ServerPlayer) (Object) this)) ci.cancel();
    }

    @SuppressWarnings("UnnecessarySuperQualifier")
    @Redirect(
            method = "hurt",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"
            )
    )
    public boolean redirectHurt(Player instance, DamageSource source, float damage) {
        var pair = VectorReflection.Server.hurtServer(instance, instance.level(), source, damage);
        if (!pair.getLeft()) {
            if (VectorReflection.Server.usesFullInstanceProtection((ServerPlayer) (Object) this)) return false;
            return super.hurt(source, pair.getRight());
        }
        var remainingDamage = pair.getRight();
        if (!(remainingDamage > 0.0f) || !Float.isFinite(remainingDamage)) return false;
        return VectorReflection.Server.usesFullInstanceProtection((ServerPlayer) (Object) this)
                ? false : super.hurt(source, remainingDamage);
    }

    @Redirect(
            method = "hurt",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;canHarmPlayer(Lnet/minecraft/world/entity/player/Player;)Z"
            )
    )
    private boolean academy$allowNonTeamDarkmatterPvp(ServerPlayer victim, Player attacker) {
        return DarkmatterTargeting.shouldBypassPvpCheck(victim, attacker)
                || victim.canHarmPlayer(attacker);
    }
}
