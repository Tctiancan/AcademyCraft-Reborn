package org.academy.api.common.ability.program;

import net.minecraft.resources.ResourceLocation;

import java.util.Set;

/**
 * Ability-category and entitlement restrictions attached to a registered node type.
 */
public record ProgramNodeScope(
        Set<ResourceLocation> allowedCategories,
        Set<ResourceLocation> requiredCapabilities
) {
    public static final ProgramNodeScope COMMON = new ProgramNodeScope(Set.of(), Set.of());

    public ProgramNodeScope {
        allowedCategories = Set.copyOf(allowedCategories);
        requiredCapabilities = Set.copyOf(requiredCapabilities);
    }

    public static ProgramNodeScope category(ResourceLocation category) {
        return new ProgramNodeScope(Set.of(category), Set.of());
    }

    public boolean allowsCategory(ResourceLocation category) {
        return allowedCategories.isEmpty() || allowedCategories.contains(category);
    }
}
