package org.academy.internal.common.ability.aeromanip.program;

import net.minecraft.resources.ResourceLocation;
import org.academy.AcademyCraft;
import org.academy.internal.common.ability.SkillNames;

/**
 * Learned-skill capabilities required by Aeromanip action nodes.
 */
public final class AeromanipProgramCapabilities {
    public static final ResourceLocation AIRFLOW_PUSH =
            AcademyCraft.academy(SkillNames.PNEUMATIC_GRASP);
    public static final ResourceLocation CONVERGING_AIRFLOW = AIRFLOW_PUSH;
    public static final ResourceLocation LAMINAR_CUT =
            AcademyCraft.academy(SkillNames.LAMINAR_CUTTER);
    public static final ResourceLocation HIGH_SPEED_JET =
            AcademyCraft.academy(SkillNames.HIGH_SPEED_JET);

    private AeromanipProgramCapabilities() {
    }
}
