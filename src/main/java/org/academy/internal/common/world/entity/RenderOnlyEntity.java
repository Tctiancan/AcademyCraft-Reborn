package org.academy.internal.common.world.entity;


import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;

public abstract class RenderOnlyEntity extends Entity {
    public RenderOnlyEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public boolean shouldRender(double d, double e, double f) {
        return true;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    protected void doWaterSplashEffect() {
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    public boolean displayFireAnimation() {
        return false;
    }

    @Override
    public boolean ignoreExplosion(Explosion explosion) {
        return true;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource damageSource, float amount) {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }
}
