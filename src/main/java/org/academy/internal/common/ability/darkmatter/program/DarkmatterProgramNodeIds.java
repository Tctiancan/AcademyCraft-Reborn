package org.academy.internal.common.ability.darkmatter.program;

import net.minecraft.resources.ResourceLocation;
import org.academy.AcademyCraft;

/**
 * Stable identifiers for Darkmatter program nodes.
 */
public final class DarkmatterProgramNodeIds {
    public static final ResourceLocation CASTER = id("target/caster");
    public static final ResourceLocation LOOK_TARGET = id("target/look_target");
    public static final ResourceLocation PHASE_STATE = id("query/phase_state");
    public static final ResourceLocation DISASSEMBLE_BLOCK = id("action/disassemble_block");
    public static final ResourceLocation DISASSEMBLE_ENTITY = id("action/disassemble_entity");
    public static final ResourceLocation DARKMATTER_CUT = id("action/darkmatter_cut");
    public static final ResourceLocation CREATE_BEETLE = id("action/create_beetle");
    public static final ResourceLocation DISASSEMBLY_FIELD = id("action/disassembly_field");

    private DarkmatterProgramNodeIds() {
    }

    private static ResourceLocation id(String path) {
        return AcademyCraft.academy("program/darkmatter/" + path);
    }
}
