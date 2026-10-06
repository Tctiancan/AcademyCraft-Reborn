package org.academy.internal.common.ability.accelerator.program;

import net.minecraft.resources.ResourceLocation;
import org.academy.AcademyCraft;

/**
 * Stable identifiers for vector-manipulation program nodes.
 */
public final class AcceleratorProgramNodeIds {
    public static final ResourceLocation CASTER = id("target/caster");
    public static final ResourceLocation LOOK_TARGET = id("target/look_target");
    public static final ResourceLocation INCOMING_PROJECTILES = id("target/incoming_projectiles");
    public static final ResourceLocation OBSERVATION_INVERSE = id("logic/observation_inverse");
    public static final ResourceLocation VECTOR_REFLECTION = id("logic/vector_reflection");
    public static final ResourceLocation APPLY_VECTOR = id("action/apply_vector");
    public static final ResourceLocation REWRITE_MOTION = id("action/rewrite_motion");
    public static final ResourceLocation KINETIC_IMPACT = id("action/kinetic_impact");
    public static final ResourceLocation KINETIC_SHOCKWAVE = id("action/kinetic_shockwave");
    public static final ResourceLocation REDIRECT_PROJECTILE = id("action/redirect_projectile");
    public static final ResourceLocation DISPLACE_ENTITY = id("action/displace_entity");
    public static final ResourceLocation DISPLACE_BLOCK = id("action/displace_block");

    private AcceleratorProgramNodeIds() {
    }

    private static ResourceLocation id(String path) {
        return AcademyCraft.academy("program/accelerator/" + path);
    }
}
