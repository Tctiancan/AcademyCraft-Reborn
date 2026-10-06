package org.academy.internal.common.ability.aeromanip.program;

import net.minecraft.resources.ResourceLocation;
import org.academy.AcademyCraft;

/**
 * Stable identifiers for Aeromanip program nodes.
 */
public final class AeromanipProgramNodeIds {
    public static final ResourceLocation CASTER = id("target/caster");
    public static final ResourceLocation LOOK_TARGET = id("target/look_target");
    public static final ResourceLocation AIRFLOW_PUSH = id("action/airflow_push");
    public static final ResourceLocation CONVERGING_AIRFLOW = id("action/converging_airflow");
    public static final ResourceLocation LAMINAR_CUT = id("action/laminar_cut");
    public static final ResourceLocation PLACE_TEMPORARY_JET_NOZZLE =
            id("action/place_temporary_jet_nozzle");
    public static final ResourceLocation FIRE_JETS = id("action/fire_jets");
    public static final ResourceLocation OWNED_JET_NOZZLE_COUNT =
            id("query/owned_jet_nozzle_count");
    public static final ResourceLocation LAUNCH_BLOCK_STRUCTURE =
            id("action/launch_block_structure");

    private AeromanipProgramNodeIds() {
    }

    private static ResourceLocation id(String path) {
        return AcademyCraft.academy("program/aeromanip/" + path);
    }
}
