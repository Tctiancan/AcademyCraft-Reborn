package org.academy.internal.common.world.entity.projectile;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.academy.internal.common.world.entity.EntityTypes;
import org.academy.internal.common.world.item.CoinItem;
import org.academy.internal.common.world.item.Items;

public class ThrownCoin extends AbstractArrow implements ItemSupplier {
    private static final String HEADS_TAG = "Heads";
    public int angle;
    public int angleOld;
    private boolean heads;

    public ThrownCoin(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
    }

    public ThrownCoin(Level level, LivingEntity shooter) {
        super(EntityTypes.THROWN_COIN.get(), shooter, level, new ItemStack(Items.COIN), null);
    }

    @Override
    public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
        if (!level().isClientSide()) {
            var initialDirection = new Vec3(x, y, z).normalize();
            var finalVelocity = initialDirection.scale(velocity);
            setDeltaMovement(finalVelocity);
            setRot((float) ((Mth.atan2(initialDirection.x, initialDirection.z)) * Mth.RAD_TO_DEG),
                    (float) ((Mth.atan2(initialDirection.y, initialDirection.horizontalDistance())) * Mth.RAD_TO_DEG));
            yRotO = getYRot();
            xRotO = getXRot();
        }
    }

    @Override
    public void tick() {
        super.tick();
        angleOld = angle;
        angle++;
    }

    public boolean isHeads() {
        return heads;
    }

    public void setHeads(boolean heads) {
        this.heads = heads;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean(HEADS_TAG, heads);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        heads = tag.contains(HEADS_TAG, CompoundTag.TAG_BYTE) ? tag.getBoolean(HEADS_TAG) : false;
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(Items.COIN);
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
    }

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {
        if (level() instanceof ServerLevel serverLevel) {
            var droppedCoin = spawnAtLocation(getPickupItem(), 0.1F);
            var owner = getOwner();
            if (droppedCoin != null) {
                droppedCoin.getPersistentData().putBoolean(CoinItem.HEADS_TAG, heads);
                if (owner != null) {
                    droppedCoin.setTarget(owner.getUUID());
                    droppedCoin.setThrower(owner);
                }
            }
            discard();
        }
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return getItem();
    }
}
