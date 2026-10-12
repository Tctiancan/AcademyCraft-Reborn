package org.academy.mixin.client;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import org.academy.api.common.ability.ImagineBreakerHealthAccess;
import org.academy.internal.client.ability.VectorReflectionClientRuntime;
import org.academy.internal.client.ability.mentalout.PlayerControlClientState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class MixinLocalPlayer extends AbstractClientPlayer implements ImagineBreakerHealthAccess {
    @Unique private float academy$lastSentXxa;
    @Unique private float academy$lastSentZza;
    @Unique private boolean academy$lastSentJump;
    @Unique private boolean academy$lastSentShift;
    @Unique private boolean academy$lastSentInitialized;

    protected MixinLocalPlayer(ClientLevel level, GameProfile gameProfile) {
        super(level, gameProfile);
    }

    @Override
    public void imaginebreaker(float amount) {
        VectorReflectionClientRuntime.imaginebreaker((LocalPlayer) (Object) this, amount);
    }

    /**
     * 26.2 的 {@link LocalPlayer#tick()} 会在输入变化时无条件上送 {@code ServerboundPlayerInputPacket}，
     * 使服务端能经 {@code ServerPlayer.getLastClientInput()} 看到站立玩家的移动按键；1.21.1 只在乘坐
     * 载具时发送该包。站立（未乘骑）时若不转发，电磁场等按移动输入求解的服务端逻辑将一直读到
     * 空输入，磁悬浮横向速度被清零。这里是 26.2 该行为的 1.21.1 等价实现：包仅在按键状态变化时发送，
     * 且 1.21.1 服务端 {@code ServerPlayer.setPlayerInput} 对非乘骑玩家不产生任何副作用。
     */
    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;sendPosition()V"))
    private void academy$relayStandingPlayerInput(CallbackInfo ci) {
        var player = (LocalPlayer) (Object) this;
        var input = player.input;
        boolean changed = !academy$lastSentInitialized
                || academy$lastSentXxa != player.xxa
                || academy$lastSentZza != player.zza
                || academy$lastSentJump != input.jumping
                || academy$lastSentShift != input.shiftKeyDown;
        if (!changed) return;
        player.connection.send(new ServerboundPlayerInputPacket(
                player.xxa, player.zza, input.jumping, input.shiftKeyDown));
        academy$lastSentInitialized = true;
        academy$lastSentXxa = player.xxa;
        academy$lastSentZza = player.zza;
        academy$lastSentJump = input.jumping;
        academy$lastSentShift = input.shiftKeyDown;
    }

    @Inject(
            method = "aiStep",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/neoforged/neoforge/client/ClientHooks;onMovementInputUpdate(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/client/player/Input;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void academy$applyAuthorizedPlayerControlInputBeforeActions(CallbackInfo ci) {
        var player = (LocalPlayer) (Object) this;
        PlayerControlClientState.applyAuthorizedInput(player);
        if (PlayerControlClientState.prepareAuthorizedFlight(player)) {
            // An authorized vertical input is not a physical double-tap request. Keeping this at
            // zero prevents jump pulses near the target altitude from toggling flight back off.
            this.jumpTriggerTime = 0;
        }
    }
}
