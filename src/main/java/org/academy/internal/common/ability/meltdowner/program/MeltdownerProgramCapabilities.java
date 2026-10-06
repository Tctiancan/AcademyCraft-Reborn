package org.academy.internal.common.ability.meltdowner.program;

import net.minecraft.resources.ResourceLocation;
import org.academy.AcademyCraft;
import org.academy.internal.common.ability.SkillNames;

/**
 * Learned-skill capabilities required by Meltdowner action nodes.
 */
public final class MeltdownerProgramCapabilities {
    public static final ResourceLocation ELECTRON_BEAM =
            AcademyCraft.academy(SkillNames.SINGLE_HIGH_SPEED_ELECTRON_BEAM);
    public static final ResourceLocation ELECTRON_FAN = ELECTRON_BEAM;
    public static final ResourceLocation MINING_BEAM = AcademyCraft.academy(SkillNames.MINING_BEAM);
    public static final ResourceLocation ATOMIC_JET = AcademyCraft.academy(SkillNames.JET_STRIKE);

    private MeltdownerProgramCapabilities() {
    }
}
