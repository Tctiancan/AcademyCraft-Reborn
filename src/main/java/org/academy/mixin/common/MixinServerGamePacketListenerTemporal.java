package org.academy.mixin.common;

import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.academy.api.common.ability.Input;
import org.academy.api.server.vanilla.MinecraftServerContext;
import org.academy.api.server.vanilla.PlayerInputTracker;
import org.academy.internal.server.time.TemporalRuntime;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ServerGamePacketListenerImpl.class, priority = 1900)
public abstract class MixinServerGamePacketListenerTemporal {
    private static final String ENSURE_MAIN_THREAD =
            "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/server/level/ServerLevel;)V";

    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;doTick()V"
            )
    )
    private void academy$dispatchTemporalPlayerSimulation(ServerPlayer player) {
        var runtime = academy$runtime(player);
        if (runtime == null) {
            player.doTick();
            return;
        }
        runtime.dispatchPlayerSimulationTicks(player, player::doTick);
    }

    @Inject(
            method = "handleMovePlayer",
            at = @At(
                    value = "INVOKE",
                    target = ENSURE_MAIN_THREAD,
                    shift = At.Shift.AFTER
            ),
            cancellable = true
    )
    private void academy$rejectMovementDuringTemporalPause(
            ServerboundMovePlayerPacket packet,
            CallbackInfo ci
    ) {
        var listener = (ServerGamePacketListenerImpl) (Object) this;
        var player = listener.player;
        var runtime = academy$runtime(player);
        if (runtime == null || !runtime.isPlayerSimulationPaused(player)) return;
        listener.teleport(
                player.getX(),
                player.getY(),
                player.getZ(),
                player.getYRot(),
                player.getXRot()
        );
        ci.cancel();
    }

    @Inject(
            method = "handlePlayerInput",
            at = @At(
                    value = "INVOKE",
                    target = ENSURE_MAIN_THREAD,
                    shift = At.Shift.AFTER
            ),
            cancellable = true
    )
    private void academy$rejectInputDuringTemporalPause(
            ServerboundPlayerInputPacket packet,
            CallbackInfo ci
    ) {
        var player = ((ServerGamePacketListenerImpl) (Object) this).player;
        var runtime = academy$runtime(player);
        if (runtime != null && runtime.isPlayerSimulationPaused(player)) {
            ci.cancel();
        }
    }

    private TemporalRuntime academy$runtime(ServerPlayer player) {
        var context = (MinecraftServerContext) player.level().getServer();
        if (!context.hasAcademyCraftServer()) return null;
        return (TemporalRuntime) context.getAcademyCraftServer()
                .getTemporalService();
    }

    /**
     * 26.2 由服务端原生把客户端输入包写入 {@code ServerPlayer.lastClientInput}；
     * 1.21.1 无此行为，这里按 1.21.1 {@code ServerboundPlayerInputPacket} 的
     * impulse/标志重建 {@code Input} 存入附件，供 mod 的 mental out / 电磁场系统读取。
     */
    @Inject(method = "handlePlayerInput", at = @At("HEAD"))
    private void academy$trackLastClientInput(
            ServerboundPlayerInputPacket packet,
            CallbackInfo ci
    ) {
        var player = ((ServerGamePacketListenerImpl) (Object) this).player;
        if (player == null) return;
        var xxa = packet.getXxa();
        var zza = packet.getZza();
        PlayerInputTracker.set(player, new Input(
                zza > 0.0f,
                zza < 0.0f,
                xxa > 0.0f,
                xxa < 0.0f,
                packet.isJumping(),
                packet.isShiftKeyDown(),
                false
        ));
    }
}
