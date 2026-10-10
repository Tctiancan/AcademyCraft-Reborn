package org.academy.mixin.common;

import net.minecraft.server.MinecraftServer;
import org.academy.AcademyCraftConfig;
import org.academy.AcademyCraftServer;
import org.academy.api.server.vanilla.MinecraftServerContext;
import org.academy.internal.common.ability.accelerator.reflection.VectorReflectionRuntime;
import org.academy.internal.server.time.TemporalRuntime;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(value = MinecraftServer.class, priority = 2000)
public abstract class MixinMinecraftServer implements MinecraftServerContext {
    @Unique
    @Nullable
    private AcademyCraftServer academyCraftServer;

    @Unique
    @Nullable
    private volatile AcademyCraftConfig academyCraftServerConfig;

    @Override
    public AcademyCraftConfig getAcademyCraftServerConfig() {
        var config = academyCraftServerConfig;
        if (config == null) {
            synchronized (this) {
                config = academyCraftServerConfig;
                if (config == null) {
                    config = AcademyCraftServer.loadServerConfig(getMinecraftServer().getServerDirectory());
                    academyCraftServerConfig = config;
                }
            }
        }
        return config;
    }

    @Inject(method = "halt", at = @At("HEAD"))
    private void halt(boolean wait, CallbackInfo ci) {
        VectorReflectionRuntime.shutdown();
        getAcademyCraftServer().getAbilitySystemServer().halt();
    }

    @Inject(method = "tickServer", at = @At("HEAD"))
    private void academy$beginTemporalHeartbeat(
            BooleanSupplier haveTime,
            CallbackInfo ci
    ) {
        var runtime = academy$temporalRuntime();
        if (runtime != null) runtime.beginServerHeartbeat();
    }

    @Inject(method = "tickServer", at = @At("TAIL"))
    private void academy$finishTemporalHeartbeat(
            BooleanSupplier haveTime,
            CallbackInfo ci
    ) {
        var runtime = academy$temporalRuntime();
        if (runtime != null) runtime.finishServerHeartbeat();
    }

    // 1.21.1 无 26.2 的 ServerClockManager。原 @Redirect(target=ServerClockManager.tick)
    // 用于在 tickChildren 内按逻辑刻度回放服务端时钟，属于时间机器功能。1.21.1 需改用
    // tickServer 心跳钩子(demo采于上方 academy$beginTemporalHeartbeat 等)驱动，此处待定集成。
    @Inject(method = "waitUntilNextTick", at = @At("RETURN"), require = 0)
    private void academy$compensateTemporalWallClockDebt(CallbackInfo ci) {
        var runtime = academy$temporalRuntime();
        if (runtime != null) runtime.compensateWallClockDebt();
    }

    @Unique
    @Nullable
    private TemporalRuntime academy$temporalRuntime() {
        if (academyCraftServer == null) return null;
        return (TemporalRuntime) academyCraftServer.getTemporalService();
    }

    @Override
    public boolean hasAcademyCraftServer() {
        return academyCraftServer != null;
    }

    @Override
    public AcademyCraftServer getAcademyCraftServer() {
        if (academyCraftServer == null) throw new IllegalStateException(
                "AcademyCraftServer has not been initialized."
        );
        return academyCraftServer;
    }

    @Override
    public void setAcademyCraftServer(AcademyCraftServer academyCraftServer) {
        this.academyCraftServer = academyCraftServer;
    }

    @Override
    public MinecraftServer getMinecraftServer() {
        return (MinecraftServer) (Object) this;
    }
}
