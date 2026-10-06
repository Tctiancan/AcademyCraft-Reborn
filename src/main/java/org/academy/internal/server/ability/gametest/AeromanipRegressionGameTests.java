package org.academy.internal.server.ability.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import org.academy.AcademyCraft;
import org.academy.api.common.ability.AirMobility;
import org.academy.api.server.world.WaterSuppression;

import java.util.UUID;

/** Runtime checks for block restoration, fluid mixins and movement priority. */
@GameTestHolder(AcademyCraft.MOD_ID)
public final class AeromanipRegressionGameTests {
    @GameTest(template = "empty", timeoutTicks = 200)
    static void aeromanipWaterOverlap(GameTestHelper helper) {
        overlap(helper);
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    static void aeromanipWaterFlow(GameTestHelper helper) {
        flow(helper);
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    static void aeromanipWaterReload(GameTestHelper helper) {
        reload(helper);
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    static void aeromanipMovementPriority(GameTestHelper helper) {
        movement(helper);
    }

    private static void overlap(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(3, 3, 3));
        var waterlogged = Blocks.OAK_SLAB.defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, true);
        level.setBlockAndUpdate(pos, waterlogged);
        level.setBlockAndUpdate(pos.east(), Blocks.WATER.defaultBlockState());
        var first = UUID.randomUUID();
        var second = UUID.randomUUID();
        WaterSuppression.refresh(level, first, Vec3.atCenterOf(pos), 2.0);
        WaterSuppression.refresh(level, second, Vec3.atCenterOf(pos), 2.0);
        helper.assertTrue(!level.getBlockState(pos).getValue(BlockStateProperties.WATERLOGGED), "Slab must be drained");
        WaterSuppression.release(level, first);
        helper.assertTrue(level.getFluidState(pos).isEmpty(), "One lease must not restore another bubble's water");
        // A player edit made inside the bubble must survive restoration.
        level.setBlockAndUpdate(pos.east(), Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(pos, level.getBlockState(pos).setValue(BlockStateProperties.SLAB_TYPE,
                SlabType.TOP));
        WaterSuppression.release(level, second);
        helper.assertTrue(level.getBlockState(pos).equals(waterlogged.setValue(BlockStateProperties.SLAB_TYPE,
                SlabType.TOP)), "Original waterlogged slab must return");
        helper.assertTrue(level.getBlockState(pos.east()).is(Blocks.STONE), "Restoration must preserve player edits");
        helper.succeed();
    }

    private static void flow(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(4, 3, 4));
        for (var floor : BlockPos.betweenClosed(pos.offset(-3, -1, -3), pos.offset(3, -1, 3)))
            level.setBlockAndUpdate(floor, Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(pos, Blocks.WATER.defaultBlockState());
        level.setBlockAndUpdate(pos.east(2), Blocks.WATER.defaultBlockState());
        var lease = UUID.randomUUID();
        WaterSuppression.refresh(level, lease, Vec3.atCenterOf(pos), 1.0);
        // Exercise the actual mixin entry point before any refresh can remove incoming water.
        try {
            var spread = FlowingFluid.class.getDeclaredMethod("spreadTo",
                    LevelAccessor.class, BlockPos.class,
                    BlockState.class, Direction.class,
                    FluidState.class);
            spread.setAccessible(true);
            spread.invoke(Fluids.FLOWING_WATER, level, pos.east(),
                    level.getBlockState(pos.east()), Direction.WEST,
                    Fluids.FLOWING_WATER.defaultFluidState());
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Cannot exercise the runtime fluid-spread entry point", exception);
        }
        helper.assertTrue(level.getFluidState(pos.east()).isEmpty(), "Fluid spread mixin must reject incoming water immediately");
        for (var tick = 1; tick <= 15; tick++) {
            helper.runAfterDelay(tick, () -> WaterSuppression.refresh(level, lease, Vec3.atCenterOf(pos), 1.0));
        }
        helper.runAfterDelay(12, () -> {
            helper.assertTrue(level.getFluidState(pos.east()).isEmpty(), "Neighbouring water must not flow into the dry pocket");
            helper.assertTrue(WaterSuppression.suppliesAir(level, Vec3.atCenterOf(pos)), "Displaced water must count as artificial air");
        });
        // Stop renewing: abandoned leases must restore water without a live owner.
        helper.runAfterDelay(22, () -> {
            helper.assertTrue(level.getBlockState(pos).is(Blocks.WATER), "Expired bubble must restore its source water");
            helper.assertTrue(!WaterSuppression.contains(level, pos), "Expired lease must stop blocking water");
            helper.succeed();
        });
    }

    private static void reload(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(3, 3, 3));
        var original = Blocks.OAK_SLAB.defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, true);
        level.setBlockAndUpdate(pos, original);
        WaterSuppression.refresh(level, UUID.randomUUID(), Vec3.atCenterOf(pos), 2.0);
        var data = level.getDataStorage()
                .computeIfAbsent(WaterSuppression.getType(), AcademyCraft.academy("suppressed_water").toString());
        var encoded = WaterSuppression.CODEC.encodeStart(NbtOps.INSTANCE, data).getOrThrow();
        var reloaded = WaterSuppression.CODEC.parse(NbtOps.INSTANCE, encoded).getOrThrow();
        level.getDataStorage().set(AcademyCraft.academy("suppressed_water").toString(), reloaded);
        helper.runAfterDelay(2, () -> {
            helper.assertTrue(level.getBlockState(pos).equals(original), "Reloaded restoration journal must restore the original slab without an owner");
            helper.succeed();
        });
    }

    private static void movement(GameTestHelper helper) {
        var entity = helper.spawn(EntityType.ARMOR_STAND, 3, 4, 3);
        entity.setOnGround(false);
        entity.setDeltaMovement(1.5, -0.9, -0.75);
        AirMobility.setSupport(entity, AirMobility.HOVER, entity.getY());
        AirMobility.applySupport(entity);
        helper.assertTrue(entity.getDeltaMovement().equals(new Vec3(1.5, 0.0, -0.75)), "Hover must retain horizontal movement");
        AirMobility.prioritizePropulsion(entity, 20);
        entity.setDeltaMovement(1.5, -2.0, -0.75);
        AirMobility.setSupport(entity, AirMobility.SLOW_FALL, 0.0);
        AirMobility.applySupport(entity);
        helper.assertTrue(entity.getDeltaMovement().y == -2.0, "Slow fall must not brake active propulsion");
        helper.succeed();
    }
}
