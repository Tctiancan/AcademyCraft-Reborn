package org.academy.internal.common.world.entity.projectile;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.academy.api.common.damage.AbilityHitEffects;
import org.academy.api.common.damage.SkillDamageSource;
import org.academy.internal.common.ability.Skills;
import org.academy.internal.common.ability.darkmatter.targeting.DarkmatterTargeting;
import org.academy.internal.common.ability.level0.skills.OutputControl;
import org.academy.internal.common.world.item.Items;

import java.util.UUID;

/**
 * Short-lived, server-owned feather blade used by Dark Matter Interference.
 */
public final class DarkmatterFeatherProjectile extends AbstractArrow implements ItemSupplier {
    private static final EntityDataAccessor<Boolean> PASSED_TARGET = SynchedEntityData.defineId(
            DarkmatterFeatherProjectile.class, EntityDataSerializers.BOOLEAN);
    private UUID targetId;
    private float damage = 1.0f;
    private float exposureBurstDamage;
    private int maximumLifetime = 40;
    private boolean outputAdjustmentBypassed;
    private int exitTicks;

    public DarkmatterFeatherProjectile(EntityType<? extends AbstractArrow> type, Level level) {
        super(type, level);
        pickup = Pickup.DISALLOWED;
        setNoGravity(true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(PASSED_TARGET, false);
    }

    public boolean hasPassedTarget() { return entityData.get(PASSED_TARGET); }

    @Override
    protected boolean canHitEntity(Entity entity) {
        // The short exit is presentation only: no second hit or unrelated target damage.
        return !hasPassedTarget() && super.canHitEntity(entity);
    }

    public void configure(
            ServerPlayer owner,
            LivingEntity target,
            Vec3 direction,
            float damage,
            float exposureBurstDamage
    ) {
        configure(owner, target, direction, damage, exposureBurstDamage, false);
    }

    public void configure(
            ServerPlayer owner,
            LivingEntity target,
            Vec3 direction,
            float damage,
            float exposureBurstDamage,
            boolean outputAdjustmentBypassed
    ) {
        setOwner(owner);
        entityData.set(PASSED_TARGET, false);
        exitTicks = 0;
        targetId = target == null ? null : target.getUUID();
        this.damage = Math.max(0.0f, Float.isFinite(damage) ? damage : 0.0f);
        this.exposureBurstDamage = Math.max(
                0.0f, Float.isFinite(exposureBurstDamage) ? exposureBurstDamage : 0.0f);
        this.outputAdjustmentBypassed = outputAdjustmentBypassed;
        moveTo(owner.getX(), owner.getEyeY() - 0.15, owner.getZ(),
                owner.getYRot(), owner.getXRot());
        var initial = target == null
                ? direction
                : target.getBoundingBox().getCenter().subtract(position());
        if (initial == null || initial.lengthSqr() < 1.0e-8) {
            initial = owner.getLookAngle();
        }
        var velocity = initial.normalize().scale(1.65);
        setDeltaMovement(velocity);
    }

    @Override
    public void tick() {
        if (!level().isClientSide() && hasPassedTarget() && --exitTicks <= 0) {
            discard();
            return;
        }
        if (!level().isClientSide() && !hasPassedTarget() && targetId != null) {
            if (!(((ServerLevel) level()).getEntity(targetId) instanceof LivingEntity target)
                    || !target.isAlive() || target.isRemoved()) {
                // A targeted feather belongs to that attack. Letting it continue after its
                // target disappears makes it damage an unrelated entity that happens to enter
                // the old trajectory, which also breaks per-target exposure accounting.
                discard();
                return;
            }
            var desired = target.getBoundingBox().getCenter().subtract(position());
            if (desired.lengthSqr() > 1.0e-8) {
                var blended = getDeltaMovement().normalize().scale(0.65)
                        .add(desired.normalize().scale(0.35));
                if (blended.lengthSqr() > 1.0e-8) {
                    setDeltaMovement(blended.normalize().scale(1.65));
                }
            }
        }
        super.tick();
        if (!level().isClientSide() && tickCount >= maximumLifetime) discard();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (level().isClientSide()) return;
        // A single arrow step can already contain several overlapping hit results.
        if (hasPassedTarget()) return;
        if (!(level() instanceof ServerLevel level)
                || !(getOwner() instanceof ServerPlayer owner)
                || !(result.getEntity() instanceof LivingEntity target)
                || !DarkmatterTargeting.isAttackableBy(owner, target)) {
            discard();
            return;
        }
        var source = SkillDamageSource.of(owner, Skills.DARKMATTER_RADIATION.get());
        target.invulnerableTime = 0;
        var hit = outputAdjustmentBypassed
                ? OutputControl.callWithoutOutputAdjustment(() -> hurtTarget(
                level, target, source, damage, exposureBurstDamage))
                : hurtTarget(level, target, source, damage, exposureBurstDamage);
        if (hit) {
            Skills.DARKMATTER_RADIATION.get().reportActivity(owner, true);
            org.academy.api.server.ability.DarkmatterGraphEffects.lightContact(target);
        }
        var velocity = getDeltaMovement();
        if (!hit || velocity.lengthSqr() < 1.0e-8) { discard(); return; }
        var forward = velocity.normalize();
        var farSide = result.getLocation().add(forward.scale(target.getBbWidth() + target.getBbHeight() + 2));
        var exit = target.getBoundingBox().inflate(0.1).clip(farSide, result.getLocation()).orElse(target.position());
        exitTicks = Math.clamp(1 + (int) Math.ceil((result.getLocation().distanceTo(exit) + 1.5) / velocity.length()), 2, 16);
        entityData.set(PASSED_TARGET, true);
        targetId = null;
        setDeltaMovement(forward.scale(1.65));
    }

    private static boolean hurtTarget(
            ServerLevel level,
            LivingEntity target,
            SkillDamageSource source,
            float damage,
            float exposureBurstDamage
    ) {
        var hit = damage > 0.0f && DarkmatterTargeting.hurt(level, target, source, damage);
        if (exposureBurstDamage > 0.0f && target.isAlive()) {
            target.invulnerableTime = 0;
            hit |= AbilityHitEffects.detonateLaw(target, source, exposureBurstDamage);
        }
        return hit;
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (!level().isClientSide()) discard();
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(Items.DARKMATTER_FEATHER.get());
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return getItem();
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("academy_target", CompoundTag.TAG_STRING)) {
            try {
                targetId = UUID.fromString(tag.getString("academy_target"));
            } catch (IllegalArgumentException ignored) {
                targetId = null;
            }
        }
        damage = (tag.contains("academy_damage_milli", CompoundTag.TAG_INT) ? tag.getInt("academy_damage_milli") : 1_000) / 1_000.0f;
        exposureBurstDamage = (tag.contains("academy_exposure_burst_milli", CompoundTag.TAG_INT) ? tag.getInt("academy_exposure_burst_milli") : 0) / 1_000.0f;
        maximumLifetime = Math.max(5, tag.contains("academy_maximum_lifetime", CompoundTag.TAG_INT) ? tag.getInt("academy_maximum_lifetime") : 40);
        outputAdjustmentBypassed = (tag.contains("academy_output_adjustment_bypassed", CompoundTag.TAG_INT) ? tag.getInt("academy_output_adjustment_bypassed") : 0) != 0;
        exitTicks = Math.clamp(tag.contains("academy_exit_ticks", CompoundTag.TAG_INT) ? tag.getInt("academy_exit_ticks") : 0, 0, 16);
        entityData.set(PASSED_TARGET, exitTicks > 0);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (targetId != null) tag.putString("academy_target", targetId.toString());
        tag.putInt("academy_damage_milli", Math.round(damage * 1_000.0f));
        tag.putInt("academy_exposure_burst_milli",
                Math.round(exposureBurstDamage * 1_000.0f));
        tag.putInt("academy_maximum_lifetime", maximumLifetime);
        tag.putInt("academy_output_adjustment_bypassed",
                outputAdjustmentBypassed ? 1 : 0);
        tag.putInt("academy_exit_ticks", exitTicks);
    }
}
