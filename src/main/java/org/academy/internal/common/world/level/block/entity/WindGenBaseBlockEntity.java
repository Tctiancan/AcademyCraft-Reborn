package org.academy.internal.common.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.academy.api.common.wireless.WirelessUser;
import org.academy.internal.client.definitions.WindGenBaseAnimation;
import org.academy.internal.client.gui.world.WindGenWorldGui;
import org.academy.internal.common.util.EnergyChargeHelper;
import org.academy.internal.common.world.level.block.Blocks;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public final class WindGenBaseBlockEntity extends MultiBlockEntity implements Container, WirelessUser {
    private final EnergyUpdateThrottle energyUpdates = new EnergyUpdateThrottle();
    private static final String NBT_COMPLETENESS = "Completeness";
    private static final int OUTPUT_TRANSFER_RATE = 1_000;
    public final AnimationState setupState = new AnimationState();
    public final AnimationState shutdownState = new AnimationState();
    public int ticks;
    public int energyStored;
    @Nullable
    public WindGenWorldGui windGenWorldGUI;
    public Completeness completeness = Completeness.BASE_ONLY;
    @Nullable
    public WindGenTopBlockEntity topBlockEntity;
    public int altitude;
    public boolean isDisplayActive = false;
    private NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);
    @Nullable
    private BlockPos connectedNodePos = null;
    private boolean playerNearby = true;

    public WindGenBaseBlockEntity(BlockPos pos, BlockState blockState) {
        super(BlockEntityTypes.WIND_GEN_BASE.get(), pos, blockState);
        if (isMain()) {
            windGenWorldGUI = new WindGenWorldGui();
            windGenWorldGUI.onInit();
            shutdownState.start(ticks);
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, WindGenBaseBlockEntity blockEntity) {
        blockEntity.ticks++;

        if (level.isClientSide()) {
            tickClient(level, pos, state, blockEntity);
        }
        if (level instanceof ServerLevel serverLevel) {
            tickServer(serverLevel, pos, state, blockEntity);
        }
    }

    public static void tickClient(Level level, BlockPos pos, BlockState state, WindGenBaseBlockEntity blockEntity) {
        blockEntity.clientTick();
    }

    public static void tickServer(ServerLevel level, BlockPos pos, BlockState state, WindGenBaseBlockEntity blockEntity) {
        blockEntity.serverTick();
    }

    private void serverTick() {
        energyUpdates.flush(this, energyStored);
        if (!isMain()) return;
        updateState();
        if (level != null && completeness == Completeness.COMPLETE && topBlockEntity != null && topBlockEntity.hasFan) {
            setEnergyStorage(energyStored + GeneratorOutput.WIND_PER_TICK);
        }
        if (EnergyChargeHelper.transferToItem(this, this, 0, OUTPUT_TRANSFER_RATE) > 0) {
            setChanged();
        }
    }

    private void clientTick() {
        if (isMain() && level != null) {
            var wasNearby = playerNearby;
            var nearbyPlayers = level.getNearestPlayer(
                    worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), 10, false
            );
            playerNearby = nearbyPlayers != null && energyStored > 0;

            if (wasNearby != playerNearby) {
                handleStateChangeOnClient(wasNearby, playerNearby);
            }
        }
    }

    private void handleStateChangeOnClient(boolean wasNearby, boolean isNearby) {
        if (wasNearby == isNearby) return;

        var currentAnimationState = wasNearby ? setupState : shutdownState;
        var targetAnimationState = isNearby ? setupState : shutdownState;
        var targetAnimationDefinition = isNearby ? WindGenBaseAnimation.SETUP : WindGenBaseAnimation.SHUT;

        if (!isNearby) {
            isDisplayActive = false;
        }

        var elapsedMillis = currentAnimationState.getTimeInMillis(ticks);
        currentAnimationState.stop();

        if (elapsedMillis > 0) {
            var elapsedSeconds = elapsedMillis / 1000.0f;
            var totalDuration = targetAnimationDefinition.lengthInSeconds();
            var targetStartSeconds = Mth.clamp(totalDuration - elapsedSeconds, 0.0f, totalDuration);
            var targetElapsedTicks = (long) (targetStartSeconds * 20.0f);
            var adjustedStartTick = ticks - targetElapsedTicks;
            targetAnimationState.start((int) Mth.clamp(adjustedStartTick, Integer.MIN_VALUE, Integer.MAX_VALUE));
        } else {
            targetAnimationState.start(ticks);
        }
    }

    private void updateState() {
        if (level != null) {
            var pillars = 0;
            var hasTop = false;
            var p = getBlockPos();
            var calculatedCompleteness = Completeness.BASE_ONLY;

            for (var y = p.getY() + 2; y < level.getMaxBuildHeight(); ++y) {
                var pos = new BlockPos(p.getX(), y, p.getZ());
                var currentState = level.getBlockState(pos);
                if (currentState.isAir()) {
                    y = level.getMaxBuildHeight();
                }
                var block = currentState.getBlock();

                if (block == Blocks.WIND_GEN_PILLAR.get()) {
                    ++pillars;
                    if (pillars > 64) {
                        break;
                    }
                } else if (block == Blocks.WIND_GEN_TOP.get()) {
                    if (currentState.hasBlockEntity()) {
                        if (level.getBlockEntity(pos) instanceof WindGenTopBlockEntity windGenTopBlockEntity) {
                            topBlockEntity = windGenTopBlockEntity;
                            hasTop = true;
                        }
                    }
                    break;
                } else {
                    break;
                }
            }

            if (hasTop) {
                calculatedCompleteness = pillars >= 20
                        ? Completeness.COMPLETE
                        : Completeness.NO_TOP;
            } else {
                if (pillars >= 20) {
                    calculatedCompleteness = Completeness.NO_TOP;
                }
            }
            if (calculatedCompleteness != completeness) {
                completeness = calculatedCompleteness;
                setChanged();
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
            }
            if (topBlockEntity != null) {
                altitude = topBlockEntity.getBlockPos().getY();
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString(NBT_COMPLETENESS, completeness.name());
        ContainerHelper.saveAllItems(tag, items, registries);
        if (isMain()) {
            tag.putInt("energy_stored", energyStored);
            if (connectedNodePos != null) {
                tag.putLong("connected_node_pos", connectedNodePos.asLong());
            }
            tag.putInt("altitude", altitude);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
        completeness = tag.contains(NBT_COMPLETENESS, CompoundTag.TAG_STRING)
                ? Completeness.valueOf(tag.getString(NBT_COMPLETENESS))
                : Completeness.BASE_ONLY;

        if (isMain()) {
            energyStored = tag.contains("energy_stored", CompoundTag.TAG_INT) ? tag.getInt("energy_stored") : 0;
            connectedNodePos = null;
            if (tag.contains("connected_node_pos", CompoundTag.TAG_LONG)) {
                connectedNodePos = BlockPos.of(tag.getLong("connected_node_pos"));
            }
            altitude = tag.contains("altitude", CompoundTag.TAG_INT) ? tag.getInt("altitude") : 0;
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        var level = getLevel();
        if (level != null) {
            Containers.dropContents(level, getBlockPos(), this);
            level.updateNeighbourForOutputSignal(getBlockPos(), getBlockState().getBlock());
        }
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        var itemstack = ContainerHelper.removeItem(items, slot, amount);
        if (!itemstack.isEmpty()) {
            setChanged();
        }

        return itemstack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    @Override
    public @Nullable BlockPos getConnectedNodePosition() {
        if (!isMain() && level != null && mainPos != null) {
            var mainBE = getMain();
            if (mainBE instanceof WindGenBaseBlockEntity mainWindGen) {
                return mainWindGen.getConnectedNodePosition();
            }
            return null;
        }
        return connectedNodePos;
    }

    @Override
    public void setConnectedNodePosition(@Nullable BlockPos nodePos) {
        if (!isMain() && level != null && mainPos != null) {
            var mainBE = getMain();
            if (mainBE instanceof WindGenBaseBlockEntity mainWindGen) {
                mainWindGen.setConnectedNodePosition(nodePos);
            }
            return;
        }
        if (!Objects.equals(connectedNodePos, nodePos)) {
            connectedNodePos = nodePos;
            setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL);
            }
        }
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        var energyStored = getEnergyStored();
        var energyToExtract = Math.min(maxExtract, energyStored);
        if (energyToExtract <= 0) {
            return 0;
        }
        if (!simulate) {
            setEnergyStorage(energyStored - energyToExtract);
        }
        return energyToExtract;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        return 0;
    }

    @Override
    public int getEnergyStored() {
        return energyStored;
    }

    @Override
    public int getMaxEnergyStorage() {
        return 4800_000;
    }

    public void setEnergyStorage(int energyStorage) {
        var clamped = Mth.clamp(energyStorage, 0, getMaxEnergyStorage());
        if (clamped != energyStored) {
            energyStored = clamped;
            setChanged();
            if (level != null && !level.isClientSide()) {
                energyUpdates.markChanged();
            }
        }
    }

    public AABB getRenderBoundingBox() {
        var pos = Vec3.atCenterOf(getBlockPos());
        var radius = 1.5f;
        return new AABB(pos.x - radius, pos.y - radius, pos.z - radius, pos.x + radius, pos.y + radius, pos.z + radius);
    }

    public enum Completeness {
        BASE_ONLY, NO_TOP, COMPLETE, COMPLETE_NOT_WORKING
    }
}
