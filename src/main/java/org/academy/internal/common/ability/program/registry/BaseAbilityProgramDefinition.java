package org.academy.internal.common.ability.program.registry;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceLocation;
import org.academy.api.common.ability.program.*;
import org.academy.internal.common.ability.program.editor.ProgramEditorNodeCatalog;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Baseline programmable surface for an ability category that has not installed action nodes yet.
 * It owns a category-isolated cast entry and the complete common program algebra.
 */
public final class BaseAbilityProgramDefinition {
    private BaseAbilityProgramDefinition() {
    }

    public static AbilityProgramDefinition create(ResourceLocation category) {
        Objects.requireNonNull(category, "category");
        var entryId = entryId(category);
        var entryType = entryType(category);
        var editor = ProgramEditorNodeCatalog.builder(category)
                .includeCommonNodes()
                .add(
                        entryId,
                        entryType,
                        new JsonObject(),
                        ProgramEditorNodeCatalog.Group.FLOW,
                        "screen.academy.program.node.on_cast",
                        "screen.academy.program.port.",
                        null
                )
                .build();
        return new AbilityProgramDefinition(
                category,
                Map.of(entryId, entryType),
                unused726 -> null,
                editor,
                ProgramLimits.DEFAULT
        );
    }

    public static ResourceLocation entryId(ResourceLocation category) {
        Objects.requireNonNull(category, "category");
        return ResourceLocation.fromNamespaceAndPath(
                category.getNamespace(),
                "program/" + category.getPath() + "/entry/on_cast"
        );
    }

    public static ProgramNodeType<?> entryType(ResourceLocation category) {
        return new EntryNodeType(Objects.requireNonNull(category, "category"));
    }

    private enum EmptyConfiguration {
        INSTANCE
    }

    private record EntryNodeType(ResourceLocation category)
            implements ProgramNodeType<EmptyConfiguration> {
        private EntryNodeType {
            Objects.requireNonNull(category, "category");
        }

        @Override
        public Codec<EmptyConfiguration> configurationCodec() {
            return MapCodec.unit(EmptyConfiguration.INSTANCE).codec();
        }

        @Override
        public int schemaVersion() {
            return 1;
        }

        @Override
        public ProgramNodeSchema schema(EmptyConfiguration configuration) {
            return new ProgramNodeSchema(
                    List.of(),
                    List.of(ProgramPortDefinition.output("flow", ProgramValueTypes.FLOW))
            );
        }

        @Override
        public ProgramNodeRole role() {
            return ProgramNodeRole.ENTRY;
        }

        @Override
        public ProgramNodePurity purity() {
            return ProgramNodePurity.PURE;
        }

        @Override
        public ProgramNodeScope scope() {
            return ProgramNodeScope.category(category);
        }
    }
}
