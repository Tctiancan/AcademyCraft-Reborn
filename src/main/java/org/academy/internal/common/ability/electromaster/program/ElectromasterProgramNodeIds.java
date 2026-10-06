package org.academy.internal.common.ability.electromaster.program;

import net.minecraft.resources.ResourceLocation;
import org.academy.AcademyCraft;

/**
 * Stable identifiers for Electromaster program nodes.
 */
public final class ElectromasterProgramNodeIds {
    public static final ResourceLocation CASTER = id("target/caster");
    public static final ResourceLocation LOOK_TARGET = id("target/look_target");
    public static final ResourceLocation CHARGEABLE_BLOCKS = id("target/chargeable_blocks");
    public static final ResourceLocation MAGNETIC_ENTITIES = id("target/magnetic_entities");
    public static final ResourceLocation ENERGY_DETECTION = id("logic/energy_detection");
    public static final ResourceLocation ENERGY_LEVEL = id("query/energy_level");
    public static final ResourceLocation REDSTONE_DETECTION = id("logic/redstone_detection");
    public static final ResourceLocation ARC_DISCHARGE = id("action/arc_discharge");
    public static final ResourceLocation CHAIN_DISCHARGE = id("action/chain_discharge");
    public static final ResourceLocation MAGNETIC_MOVE = id("action/magnetic_move");
    public static final ResourceLocation CURRENT_RECHARGE = id("action/current_recharge");

    private ElectromasterProgramNodeIds() {
    }

    private static ResourceLocation id(String path) {
        return AcademyCraft.academy("program/electromaster/" + path);
    }
}
