package org.academy.internal.common.ability.electromaster.program;

import net.minecraft.resources.ResourceLocation;
import org.academy.AcademyCraft;
import org.academy.internal.common.ability.SkillNames;

/**
 * Learned-skill capabilities required by Electromaster action nodes.
 */
public final class ElectromasterProgramCapabilities {
    public static final ResourceLocation ARC_DISCHARGE =
            AcademyCraft.academy(SkillNames.ARC_GENERATE);
    public static final ResourceLocation CHAIN_DISCHARGE = ARC_DISCHARGE;
    public static final ResourceLocation MAGNETIC_MOVE =
            AcademyCraft.academy(SkillNames.MAGNET_MANIPULATION);
    public static final ResourceLocation CURRENT_RECHARGE =
            AcademyCraft.academy(SkillNames.CURRENT_RECHARGE);

    private ElectromasterProgramCapabilities() {
    }
}
