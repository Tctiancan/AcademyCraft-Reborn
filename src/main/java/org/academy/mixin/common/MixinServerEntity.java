package org.academy.mixin.common;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.protocol.game.VecDeltaCodec;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.world.entity.Entity;
import org.academy.internal.common.ability.teleport.targeting.TeleportSync;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ServerEntity.class)
public abstract class MixinServerEntity {
    @Shadow
    @Final
    private Entity entity;

    @Shadow
    @Final
    private Consumer<Packet<?>> broadcast;

    @Shadow
    @Final
    private VecDeltaCodec positionCodec;

    @Shadow
    private int teleportDelay;

    @Shadow
    private boolean wasOnGround;

    @Inject(method = "sendChanges", at = @At("HEAD"))
    private void academy$forceAbilityTeleportSync(CallbackInfo ci) {
        if (!TeleportSync.consumeAbsoluteSync(entity)) return;

        broadcast.accept(new ClientboundTeleportEntityPacket(entity));
        positionCodec.setBase(entity.trackingPosition());
        teleportDelay = 0;
        wasOnGround = entity.onGround();
    }
}
