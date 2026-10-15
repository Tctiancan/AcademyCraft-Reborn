package org.academy.mixin.common;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.academy.api.server.vanilla.MinecraftServerContext;
import org.academy.internal.server.time.TemporalRuntime;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Scales vanilla(random block/fluid/precipitation) ticks in {@link ServerLevel#tickChunk}.
 *
 * <p>Kept in its own mixin so it can be disabled by {@link org.academy.mixin.plugin.MixinPlugin}
 * when a conflicting mod (e.g. Lithium's {@code alloc.chunk_random.ServerLevelMixin}) is present:
 * Mixin does not allow two independent mixins to {@code @Redirect} the same invocation point in
 * {@code tickChunk}, so competing with Lithium there crashes at Bootstrap.
 */
@Mixin(value = ServerLevel.class, priority = 1900)
public abstract class MixinServerLevelTemporalScalingRandomTick {
    @Redirect(
            method = "tickChunk",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/state/BlockState;randomTick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/util/RandomSource;)V"
            )
    )
    private void academy$dispatchRandomBlockTick(
            BlockState state,
            ServerLevel level,
            BlockPos position,
            RandomSource random
    ) {
        var context = (MinecraftServerContext) level.getServer();
        if (!context.hasAcademyCraftServer()) {
            state.randomTick(level, position, random);
            return;
        }
        ((TemporalRuntime) context.getAcademyCraftServer().getTemporalService())
                .dispatchRandomBlockTick(state, level, position, random);
    }

    @Redirect(
            method = "tickChunk",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/material/FluidState;randomTick(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/util/RandomSource;)V"
            )
    )
    private void academy$dispatchRandomFluidTick(
            FluidState state,
            Level level,
            BlockPos position,
            RandomSource random
    ) {
        var context = (MinecraftServerContext) level.getServer();
        if (!context.hasAcademyCraftServer()) {
            state.randomTick(level, position, random);
            return;
        }
        ((TemporalRuntime) context.getAcademyCraftServer().getTemporalService())
                .dispatchRandomFluidTick(state, (ServerLevel) level, position, random);
    }

    @Redirect(
            method = "tickChunk",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;tickPrecipitation(Lnet/minecraft/core/BlockPos;)V"
            )
    )
    private void academy$dispatchPrecipitationTicks(
            ServerLevel level,
            BlockPos position
    ) {
        var context = (MinecraftServerContext) level.getServer();
        if (!context.hasAcademyCraftServer()) {
            level.tickPrecipitation(position);
            return;
        }
        ((TemporalRuntime) context.getAcademyCraftServer().getTemporalService())
                .dispatchPrecipitationTicks(
                        level,
                        position,
                        () -> level.tickPrecipitation(position)
                );
    }
}
