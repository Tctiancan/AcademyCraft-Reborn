package org.academy.internal.common.ability.program.registry;

import net.minecraft.resources.ResourceLocation;
import org.academy.api.common.ability.program.ProgramNodeType;
import org.academy.api.common.registries.Registries;

/**
 * Production lookup backed by the synchronized NeoForge program-node registry.
 */
public final class RegistryProgramNodeLookup implements ProgramNodeLookup {
    public static final RegistryProgramNodeLookup INSTANCE = new RegistryProgramNodeLookup();

    private RegistryProgramNodeLookup() {
    }

    @Override
    public ProgramNodeType<?> find(ResourceLocation id) {
        return Registries.PROGRAM_NODE_TYPES.get(id)
                .map(reference -> reference.value())
                .orElse(null);
    }
}
