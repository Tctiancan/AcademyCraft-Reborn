package org.academy.internal.common.ability.darkmatter.program;

import net.minecraft.resources.ResourceLocation;
import org.academy.AcademyCraft;
import org.academy.internal.common.ability.SkillNames;

/**
 * Learned-skill capabilities required by Darkmatter action nodes.
 */
public final class DarkmatterProgramCapabilities {
    public static final ResourceLocation DISASSEMBLE_BLOCK =
            AcademyCraft.academy(SkillNames.DARKMATTER_DISASSEMBLE);
    public static final ResourceLocation DISASSEMBLE_ENTITY =
            AcademyCraft.academy(SkillNames.DARKMATTER_DISASSEMBLE);
    public static final ResourceLocation DISASSEMBLY_FIELD = DISASSEMBLE_ENTITY;
    public static final ResourceLocation DARKMATTER_CUT =
            AcademyCraft.academy(SkillNames.DARKMATTER_CUT);
    public static final ResourceLocation CREATE_BEETLE =
            AcademyCraft.academy(SkillNames.DARKMATTER_CREATION);

    private DarkmatterProgramCapabilities() {
    }
}
