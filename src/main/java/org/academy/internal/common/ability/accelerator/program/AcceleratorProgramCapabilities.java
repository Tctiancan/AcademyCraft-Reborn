package org.academy.internal.common.ability.accelerator.program;

import net.minecraft.resources.ResourceLocation;
import org.academy.AcademyCraft;
import org.academy.internal.common.ability.SkillNames;

/**
 * Learned-skill capabilities required by vector-manipulation action nodes.
 */
public final class AcceleratorProgramCapabilities {
    public static final ResourceLocation APPLY_VECTOR =
            AcademyCraft.academy(SkillNames.VECTOR_ACCEL);
    public static final ResourceLocation REWRITE_MOTION = APPLY_VECTOR;
    public static final ResourceLocation KINETIC_IMPACT =
            AcademyCraft.academy(SkillNames.KINETIC_ENERGY_APPLIED);
    public static final ResourceLocation KINETIC_SHOCKWAVE = KINETIC_IMPACT;
    public static final ResourceLocation REDIRECT_PROJECTILE =
            AcademyCraft.academy(SkillNames.VECTOR_REFLECTION);
    public static final ResourceLocation DISPLACE_ENTITY = APPLY_VECTOR;
    public static final ResourceLocation DISPLACE_BLOCK = KINETIC_IMPACT;

    private AcceleratorProgramCapabilities() {
    }
}
