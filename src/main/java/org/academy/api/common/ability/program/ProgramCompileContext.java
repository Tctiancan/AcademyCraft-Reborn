package org.academy.api.common.ability.program;

import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.Set;

/**
 * Server-authoritative category and entitlement snapshot used during compilation.
 */
public record ProgramCompileContext(
        ResourceLocation category,
        Set<ResourceLocation> capabilities,
        ProgramLimits limits
) {
    public ProgramCompileContext {
        Objects.requireNonNull(category, "category");
        capabilities = Set.copyOf(capabilities);
    }
}
