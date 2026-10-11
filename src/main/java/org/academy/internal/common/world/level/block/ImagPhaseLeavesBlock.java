package org.academy.internal.common.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import org.academy.internal.common.core.particles.ParticleTypes;

public final class ImagPhaseLeavesBlock extends LeavesBlock {
    public static final MapCodec<ImagPhaseLeavesBlock> CODEC = simpleCodec(ImagPhaseLeavesBlock::new);
    public static final float leafParticleChance = 0.5F;

    public ImagPhaseLeavesBlock(Properties properties) {
        super(properties
                .strength(0.2F)
                .randomTicks()
                .noOcclusion()
                .sound(SoundType.CHAIN)
                .isValidSpawn((state, level, pos, entityType)
                        -> entityType == EntityType.OCELOT || entityType == EntityType.PARROT)
                .isSuffocating((state, level, pos) -> false)
                .isViewBlocking((state, level, pos) -> false)
                .ignitedByLava()
                .pushReaction(PushReaction.DESTROY)
                .isRedstoneConductor((state, level, pos) -> false));
    }

    @Override
    public MapCodec<ImagPhaseLeavesBlock> codec() {
        return CODEC;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        BlockPos below = pos.below();
        BlockState belowState = level.getBlockState(below);
        makeFallingLeavesParticles(level, pos, random, belowState, below);
    }

    private void makeFallingLeavesParticles(Level level, BlockPos pos, RandomSource random, BlockState belowState, BlockPos below) {
        if (!(random.nextFloat() >= leafParticleChance)) {
            if (!isFaceFull(belowState.getCollisionShape(level, below), Direction.UP)) {
                spawnFallingLeavesParticle(level, pos, random);
            }
        }
    }

    public void spawnFallingLeavesParticle(Level level, BlockPos pos, RandomSource random) {
        level.addParticle(
                ParticleTypes.IMAG_PHASE_FLUID.get(),
                pos.getX() + random.nextDouble(),
                pos.getY() - 0.05,
                pos.getZ() + random.nextDouble(),
                0.0,
                -0.015 - random.nextDouble() * 0.025,
                0.0
        );
    }
}
