package org.academy.mixin.common.item;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MaceItem;
import org.academy.internal.common.ability.aeromanip.skills.lv1.AirflowJet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MaceItem.class)
public abstract class MixinMaceItemAirflowMomentum {
    @Redirect(
            method = "getKnockbackPower",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/entity/player/Player;fallDistance:F"
            )
    )
    private static float academy$useAirflowMomentumForKnockback(Player attacker) {
        return (float) AirflowJet.Server.getEffectiveMaceFallDistance(attacker);
    }

    @Redirect(
            method = "canSmashAttack",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/entity/LivingEntity;fallDistance:F"
            )
    )
    private static float academy$useAirflowMomentumForSmashCheck(LivingEntity entity) {
        return (float) AirflowJet.Server.getEffectiveMaceFallDistance(entity);
    }

    @Redirect(
            method = "hurtEnemy",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/server/level/ServerPlayer;fallDistance:F"
            )
    )
    private float academy$useAirflowMomentumForSmashSound(ServerPlayer attacker) {
        return (float) AirflowJet.Server.getEffectiveMaceFallDistance(attacker);
    }

    @Redirect(
            method = "getAttackDamageBonus",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/entity/LivingEntity;fallDistance:F"
            )
    )
    private float academy$useAirflowMomentumForDamage(LivingEntity entity) {
        return (float) AirflowJet.Server.getEffectiveMaceFallDistance(entity);
    }

    @Inject(method = "postHurtEnemy", at = @At("TAIL"))
    private void academy$consumeAirflowMomentum(
            ItemStack stack,
            LivingEntity target,
            LivingEntity attacker,
            CallbackInfo ci
    ) {
        if (attacker instanceof ServerPlayer player
                && AirflowJet.Server.getEffectiveMaceFallDistance(player)
                > MaceItem.SMASH_ATTACK_FALL_THRESHOLD) {
            AirflowJet.Server.consumeMaceMomentum(player);
        }
    }
}
