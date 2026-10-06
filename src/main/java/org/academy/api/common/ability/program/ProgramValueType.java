package org.academy.api.common.ability.program;

import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

/**
 * Stable identifier for a value that can travel through an ability-program data edge.
 */
public record ProgramValueType(ResourceLocation id) {
    public ProgramValueType {
        Objects.requireNonNull(id, "id");
    }
}
