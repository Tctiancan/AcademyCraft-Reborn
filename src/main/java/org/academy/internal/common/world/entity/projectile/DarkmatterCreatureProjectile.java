package org.academy.internal.common.world.entity.projectile;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.academy.AcademyCraft;
import org.academy.api.common.damage.SkillDamageSource;
import org.academy.internal.common.ability.Skills;
import org.academy.internal.common.ability.darkmatter.damage.DarkmatterLawMark;
import org.academy.internal.common.ability.darkmatter.targeting.DarkmatterTargeting;
import org.academy.internal.common.ability.level0.skills.OutputControl;
import org.academy.internal.common.world.entity.ability.DarkmatterBeetle;
import org.academy.internal.common.world.item.Items;

import java.util.UUID;

/**
 * Physical direct/homing cannon round fired by a blueprint-created construct.
 */
public final class DarkmatterCreatureProjectile extends AbstractArrow implements ItemSupplier {
    private static final ResourceLocation PENETRATION_ID =
            AcademyCraft.academy("darkmatter_creature_projectile_penetration");

    private UUID creatorId;
    private UUID targetId;
    private boolean homing;
    private float damage = 1.0f;
    private float penetration;
    private float betaPower;
    private float speed = 1.2f;
    private int maximumLifetime = 60;
    private boolean outputAdjustmentBypassed;

    public DarkmatterCreatureProjectile(EntityType<? extends AbstractArrow> type, Level level) {
        super(type, level);
        pickup = Pickup.DISALLOWED;
        setNoGravity(true);
    }

    public void configure(ServerPlayer owner, DarkmatterBeetle creator, LivingEntity target,
                          boolean homing, float damage, float penetration, float betaPower,
                          float speed, boolean outputAdjustmentBypassed) {
        setOwner(owner);
        creatorId = creator.getUUID();
        targetId = target.getUUID();
        this.homing = homing;
        this.damage = finitePositive(damage);
        this.penetration = Math.clamp(finitePositive(penetration), 0.0f, 0.5f);
        this.betaPower = finitePositive(betaPower);
        this.speed = Math.max(0.4f, finitePositive(speed));
        this.outputAdjustmentBypassed = outputAdjustmentBypassed;
        maximumLifetime = Math.max(20, Math.round(48.0f / this.speed));
        moveTo(creator.getX(), creator.getEyeY(), creator.getZ(), creator.getYRot(), creator.getXRot());
        var direction = target.getBoundingBox().getCenter().subtract(position());
        if (direction.lengthSqr() < 1.0e-8) direction = creator.getLookAngle();
        setDeltaMovement(direction.normalize().scale(this.speed));
    }

    @Override
    public void tick() {
        if (homing && targetId != null && level() instanceof ServerLevel serverLevel) {
            if (!(serverLevel.getEntity(targetId) instanceof LivingEntity target)
                    || !target.isAlive() || target.isRemoved()) {
                discard();
                return;
            }
            var desired = target.getBoundingBox().getCenter().subtract(position());
            if (desired.lengthSqr() > 1.0e-8 && getDeltaMovement().lengthSqr() > 1.0e-8) {
                var turning = Math.clamp(0.18 + 0.035 * betaPower, 0.18, 0.42);
                var blended = getDeltaMovement().normalize().scale(1.0 - turning)
                        .add(desired.normalize().scale(turning));
                setDeltaMovement(blended.normalize().scale(speed));
            }
        }
        super.tick();
        if (!level().isClientSide() && tickCount >= maximumLifetime) discard();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (!(level() instanceof ServerLevel level)
                || !(getOwner() instanceof ServerPlayer owner)
                || !(result.getEntity() instanceof LivingEntity target)
                || !DarkmatterTargeting.isAttackableBy(owner, target)) {
            discard();
            return;
        }
        var hurt = outputAdjustmentBypassed
                ? OutputControl.callWithoutOutputAdjustment(() -> hurtWithPenetration(
                level,
                target,
                SkillDamageSource.of(owner, Skills.DARKMATTER_CREATION.get()),
                damage,
                penetration
        ))
                : hurtWithPenetration(
                level,
                target,
                SkillDamageSource.of(owner, Skills.DARKMATTER_CREATION.get()),
                damage,
                penetration
        );
        if (hurt) {
            target.addEffect(new MobEffectInstance(
                    MobEffects.WEAKNESS, 20 + Math.round(10.0f * betaPower), 0, false, false));
            if (homing && betaPower > 0.0f) {
                DarkmatterLawMark.apply(owner, target, betaPower,
                        40 + Math.round(20.0f * betaPower));
            }
            Skills.DARKMATTER_CREATION.get().reportActivity(owner, true);
        }
        discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (!level().isClientSide()) discard();
    }

    private static boolean hurtWithPenetration(ServerLevel level, LivingEntity target,
                                               DamageSource source,
                                               float damage, float penetration) {
        var armor = target.getAttribute(Attributes.ARMOR);
        if (armor == null || penetration <= 0.0f) {
            return DarkmatterTargeting.hurt(level, target, source, damage);
        }
        var previous = armor.getModifier(PENETRATION_ID);
        if (previous != null) armor.removeModifier(PENETRATION_ID);
        armor.addTransientModifier(new AttributeModifier(
                PENETRATION_ID, -penetration, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        try {
            return DarkmatterTargeting.hurt(level, target, source, damage);
        } finally {
            armor.removeModifier(PENETRATION_ID);
            if (previous != null) armor.addTransientModifier(previous);
        }
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(Items.DARKMATTER.get());
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return getItem();
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        creatorId = parseUuid(tag.contains("academy_creator", CompoundTag.TAG_STRING) ? tag.getString("academy_creator") : "");
        targetId = parseUuid(tag.contains("academy_target", CompoundTag.TAG_STRING) ? tag.getString("academy_target") : "");
        homing = (tag.contains("academy_homing", CompoundTag.TAG_INT) ? tag.getInt("academy_homing") : 0) != 0;
        damage = (tag.contains("academy_damage_milli", CompoundTag.TAG_INT) ? tag.getInt("academy_damage_milli") : 1_000) / 1_000.0f;
        penetration = (tag.contains("academy_penetration_milli", CompoundTag.TAG_INT) ? tag.getInt("academy_penetration_milli") : 0) / 1_000.0f;
        betaPower = (tag.contains("academy_beta_milli", CompoundTag.TAG_INT) ? tag.getInt("academy_beta_milli") : 0) / 1_000.0f;
        speed = (tag.contains("academy_speed_milli", CompoundTag.TAG_INT) ? tag.getInt("academy_speed_milli") : 1_200) / 1_000.0f;
        maximumLifetime = Math.max(20, tag.contains("academy_lifetime", CompoundTag.TAG_INT) ? tag.getInt("academy_lifetime") : 60);
        outputAdjustmentBypassed = (tag.contains("academy_output_adjustment_bypassed", CompoundTag.TAG_INT) ? tag.getInt("academy_output_adjustment_bypassed") : 0) != 0;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (creatorId != null) tag.putString("academy_creator", creatorId.toString());
        if (targetId != null) tag.putString("academy_target", targetId.toString());
        tag.putInt("academy_homing", homing ? 1 : 0);
        tag.putInt("academy_damage_milli", Math.round(damage * 1_000.0f));
        tag.putInt("academy_penetration_milli", Math.round(penetration * 1_000.0f));
        tag.putInt("academy_beta_milli", Math.round(betaPower * 1_000.0f));
        tag.putInt("academy_speed_milli", Math.round(speed * 1_000.0f));
        tag.putInt("academy_lifetime", maximumLifetime);
        tag.putInt(
                "academy_output_adjustment_bypassed", outputAdjustmentBypassed ? 1 : 0);
    }

    private static float finitePositive(float value) {
        return Float.isFinite(value) ? Math.max(0.0f, value) : 0.0f;
    }

    private static UUID parseUuid(String value) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
