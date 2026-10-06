package org.academy.internal.common.ability.meltdowner.program;

import net.minecraft.resources.ResourceLocation;
import org.academy.AcademyCraft;

/**
 * Stable identifiers for Meltdowner program nodes.
 */
public final class MeltdownerProgramNodeIds {
    public static final ResourceLocation CASTER = id("target/caster");
    public static final ResourceLocation LOOK_TARGET = id("target/look_target");
    public static final ResourceLocation ELECTRON_BEAM = id("action/electron_beam");
    public static final ResourceLocation ELECTRON_FAN = id("action/electron_fan");
    public static final ResourceLocation MINING_BEAM = id("action/mining_beam");
    public static final ResourceLocation ATOMIC_JET = id("action/atomic_jet");

    private MeltdownerProgramNodeIds() {
    }

    private static ResourceLocation id(String path) {
        return AcademyCraft.academy("program/meltdowner/" + path);
    }
}
