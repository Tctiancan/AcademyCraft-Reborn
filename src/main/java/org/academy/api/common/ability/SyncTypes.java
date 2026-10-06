package org.academy.api.common.ability;

import net.minecraft.resources.ResourceLocation;

import static org.academy.AcademyCraft.academy;

public final class SyncTypes {
    public static final ResourceLocation CP_DATA = academy("cp_data");
    public static final ResourceLocation ABILITY_CATEGORY = academy("ability_category");
    public static final ResourceLocation SKILL_DATA = academy("skill_data");
    public static final ResourceLocation PROPS_DATA = academy("props_data");
    public static final ResourceLocation DARKMATTER_STATE = academy("darkmatter_state");
    /** Tick-only subsystem route; compressed air itself is synchronized through CP_DATA. */
    public static final ResourceLocation AEROMANIP_RESOURCE = academy("aeromanip_resource");

    public static final ResourceLocation IRON_SAND_RESOURCE = academy("iron_sand_resource");

    private SyncTypes() {
    }
}
