package org.academy.internal.common.skilldata;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

/** Retains missing/future addon data verbatim until its implementation becomes available again. */
public final class UnknownSkillData extends CommonSkillData {
    private final JsonObject raw;
    private final ResourceLocation type;
    public UnknownSkillData(JsonObject raw, ResourceLocation type) {
        this.raw = raw.deepCopy();
        this.type = type;
    }
    @Override public ResourceLocation getType() { return type; }
    @Override public boolean isEnabled() { return false; }
    public JsonObject raw() { return raw.deepCopy(); }
}
