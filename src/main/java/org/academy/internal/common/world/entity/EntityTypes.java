package org.academy.internal.common.world.entity;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.academy.internal.common.world.entity.ability.DarkmatterBeetle;
import org.academy.internal.common.world.entity.projectile.*;
import org.academy.internal.common.world.entity.skill.*;
import org.academy.internal.common.world.entity.structure.BlockStructureEntity;
import org.academy.internal.common.world.entity.vehicle.CleaningRobot;

import static org.academy.AcademyCraft.MOD_ID;
import static org.academy.AcademyCraft.academy;

public class EntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<ThrownCoin>> THROWN_COIN =
            ENTITY_TYPES.register(
                    "thrown_coin", () -> EntityType.Builder.<ThrownCoin>of(
                                    ThrownCoin::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f).build(academy("thrown_coin").toString())
            );
    public static final DeferredHolder<EntityType<?>, EntityType<PaperAirplane>> PAPER_AIRPLANE =
            ENTITY_TYPES.register(
                    "paper_airplane", () -> EntityType.Builder.<PaperAirplane>of(
                                    PaperAirplane::new, MobCategory.MISC)
                            .sized(0.45f, 0.12f).clientTrackingRange(64).updateInterval(1)
                            .build(academy("paper_airplane").toString())
            );
    public static final DeferredHolder<EntityType<?>, EntityType<MagneticHook>> MAGNETIC_HOOK =
            ENTITY_TYPES.register(
                    "magnetic_hook", () -> EntityType.Builder.<MagneticHook>of(
                                    MagneticHook::new, MobCategory.MISC)
                            .sized(0.55f, 0.55f).clientTrackingRange(96).updateInterval(1)
                            .build(academy("magnetic_hook").toString())
            );
    public static final DeferredHolder<EntityType<?>, EntityType<HighSpeedJetNozzle>> HIGH_SPEED_JET_NOZZLE =
            ENTITY_TYPES.register(
                    "high_speed_jet_nozzle", () -> EntityType.Builder.<HighSpeedJetNozzle>of(
                                    HighSpeedJetNozzle::new, MobCategory.MISC)
                            .sized(0.4f, 0.4f)
                            .clientTrackingRange(96).updateInterval(1)
                            .build(academy("high_speed_jet_nozzle").toString())
            );
    public static final DeferredHolder<EntityType<?>, EntityType<RailgunRay>> RAILGUN_RAY =
            ENTITY_TYPES.register(
                    "railgun_ray", () -> EntityType.Builder.<RailgunRay>of(
                                    RailgunRay::new, MobCategory.MISC)
                            .build(academy("railgun_ray").toString())
            );
    public static final DeferredHolder<EntityType<?>, EntityType<Plasma>> PLASMA =
            ENTITY_TYPES.register(
                    "plasma", () -> EntityType.Builder.<Plasma>of(
                                    Plasma::new, MobCategory.MISC)
                            .sized(1.0f, 1.0f).clientTrackingRange(160).updateInterval(1)
                            .build(academy("plasma").toString())
            );
    public static final DeferredHolder<EntityType<?>, EntityType<Arc>> ARC =
            ENTITY_TYPES.register(
                    "arc", () -> EntityType.Builder.<Arc>of(
                                    Arc::new, MobCategory.MISC)
                            .build(academy("arc").toString())
            );
    public static final DeferredHolder<EntityType<?>, EntityType<ArcEffect>> ARC_EFFECT =
            ENTITY_TYPES.register(
                    "arc_effect", () -> EntityType.Builder.<ArcEffect>of(
                                    ArcEffect::new, MobCategory.MISC)
                            .sized(0.1f, 0.1f).clientTrackingRange(64).updateInterval(1)
                            .build(academy("arc_effect").toString())
            );
    public static final DeferredHolder<EntityType<?>, EntityType<HighSpeedElectronBeam>> HIGH_SPEED_ELECTRON_BEAM =
            ENTITY_TYPES.register(
                    "high_speed_electron_beam", () -> EntityType.Builder.<HighSpeedElectronBeam>of(
                                    HighSpeedElectronBeam::new, MobCategory.MISC)
                            .clientTrackingRange(10).updateInterval(1)
                            .build(academy("high_speed_electron_beam").toString())
            );
    public static final DeferredHolder<EntityType<?>, EntityType<MagneticWeaponBlade>> MAGNETIC_WEAPON_BLADE =
            ENTITY_TYPES.register(
                    "magnetic_weapon_blade", () -> EntityType.Builder.<MagneticWeaponBlade>of(
                                    MagneticWeaponBlade::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f).clientTrackingRange(48).updateInterval(1)
                            .build(academy("magnetic_weapon_blade").toString())
            );
    public static final DeferredHolder<EntityType<?>, EntityType<LightOrb>> LIGHT_ORB =
            ENTITY_TYPES.register(
                    "light_orb", () -> EntityType.Builder.<LightOrb>of(
                                    LightOrb::new, MobCategory.MISC)
                            .build(academy("light_orb").toString())
            );
    public static final DeferredHolder<EntityType<?>, EntityType<GlowCircle>> GLOW_CIRCLE =
            ENTITY_TYPES.register(
                    "glow_circle", () -> EntityType.Builder.<GlowCircle>of(
                                    GlowCircle::new, MobCategory.MISC)
                            .build(academy("glow_circle").toString())
            );
    public static final DeferredHolder<EntityType<?>, EntityType<KineticShockwave>> KINETIC_SHOCKWAVE =
            ENTITY_TYPES.register(
                    "kinetic_shockwave", () -> EntityType.Builder.<KineticShockwave>of(
                                    KineticShockwave::new, MobCategory.MISC)
                            .sized(0.1f, 0.1f).clientTrackingRange(64).updateInterval(1)
                            .build(academy("kinetic_shockwave").toString())
            );
    public static final DeferredHolder<EntityType<?>, EntityType<Smoke>> SMOKE =
            ENTITY_TYPES.register(
                    "smoke", () -> EntityType.Builder.<Smoke>of(
                                    Smoke::new, MobCategory.MISC)
                            .build(academy("smoke").toString())
            );
    public static final DeferredHolder<EntityType<?>, EntityType<DarkmatterCutSlash>> DARKMATTER_CUT_SLASH =
            ENTITY_TYPES.register(
                    "darkmatter_cut_slash", () -> EntityType.Builder.<DarkmatterCutSlash>of(
                                    DarkmatterCutSlash::new, MobCategory.MISC)
                            .sized(0.1f, 0.1f).clientTrackingRange(64).updateInterval(1)
                            .build(academy("darkmatter_cut_slash").toString())
            );
    public static final DeferredHolder<EntityType<?>, EntityType<DarkmatterFeatherProjectile>> DARKMATTER_FEATHER_PROJECTILE =
            ENTITY_TYPES.register(
                    "darkmatter_feather_projectile", () -> EntityType.Builder.<DarkmatterFeatherProjectile>of(
                                    DarkmatterFeatherProjectile::new, MobCategory.MISC)
                            .sized(0.18f, 0.18f).clientTrackingRange(64).updateInterval(1)
                            .build(academy("darkmatter_feather_projectile").toString())
            );
    public static final DeferredHolder<EntityType<?>, EntityType<DarkmatterCreatureProjectile>> DARKMATTER_CREATURE_PROJECTILE =
            ENTITY_TYPES.register(
                    "darkmatter_creature_projectile", () -> EntityType.Builder.<DarkmatterCreatureProjectile>of(
                                    DarkmatterCreatureProjectile::new, MobCategory.MISC)
                            .sized(0.22f, 0.22f).clientTrackingRange(96).updateInterval(1)
                            .build(academy("darkmatter_creature_projectile").toString())
            );
    public static final DeferredHolder<EntityType<?>, EntityType<DarkmatterSpearProjectile>> DARKMATTER_SPEAR_PROJECTILE =
            ENTITY_TYPES.register(
                    "darkmatter_spear_projectile", () -> EntityType.Builder.<DarkmatterSpearProjectile>of(
                                    DarkmatterSpearProjectile::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f).clientTrackingRange(64).updateInterval(1)
                            .build(academy("darkmatter_spear_projectile").toString())
            );
    public static final DeferredHolder<EntityType<?>, EntityType<DarkmatterBeetle>> DARKMATTER_BEETLE =
            ENTITY_TYPES.register(
                    "darkmatter_beetle", () -> EntityType.Builder.<DarkmatterBeetle>of(
                                    DarkmatterBeetle::new, MobCategory.MISC)
                            .sized(0.55f, 0.4f).clientTrackingRange(48).updateInterval(2)
                            .build(academy("darkmatter_beetle").toString())
            );
    public static final DeferredHolder<EntityType<?>, EntityType<CleaningRobot>> CLEANING_ROBOT =
            ENTITY_TYPES.register(
                    "cleaning_robot", () -> EntityType.Builder.<CleaningRobot>of(
                                    CleaningRobot::new, MobCategory.MISC)
                            .build(academy("cleaning_robot").toString())
            );
    public static final DeferredHolder<EntityType<?>, EntityType<BlockStructureEntity>> BLOCK_STRUCTURE =
            ENTITY_TYPES.register(
                    "block_structure", () -> EntityType.Builder.<BlockStructureEntity>of(
                                    BlockStructureEntity::new,
                                    MobCategory.MISC)
                            .sized(1.0f, 1.0f)
                            .clientTrackingRange(160)
                            .updateInterval(1)
                            .build(academy("block_structure").toString())
            );

    private EntityTypes() {
    }
}
