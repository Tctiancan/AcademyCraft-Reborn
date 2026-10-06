package org.academy.internal.common.world.entity.skill;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;

public class Arc extends Entity {
    public static final EntityDataAccessor<Float> ID_LENGTH = SynchedEntityData.defineId(Arc.class, EntityDataSerializers.FLOAT);
    public static final int defaultLifetime = 12;
    public int currentLifetime = defaultLifetime;

    public Arc(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(ID_LENGTH, 0f);
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    protected void doWaterSplashEffect() {
    }

    @Override
    public boolean hurt(DamageSource damageSource, float amount) {
        return false;
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
    public void tick() {
        super.tick();

/*        if (level().isClientSide()) {
            var style = ArcStyles.classic();
            style.seed = MathUtil.RANDOM.nextLong();
            style.start.set(0, 0, 0);
            style.end.set(0, getLength(), 0);
            renderData = ArcFactory.Generator.generate(style);
        }*/

        currentLifetime--;
        if (currentLifetime <= 0) {
            if (level() instanceof ServerLevel) {
                kill();
            }
        }
    }

    @Override
    public boolean shouldRender(double d, double e, double f) {
        return true;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }

    public float getLength() {
        return entityData.get(ID_LENGTH);
    }

    public void setLength(float length) {
        entityData.set(ID_LENGTH, length);
    }


}
