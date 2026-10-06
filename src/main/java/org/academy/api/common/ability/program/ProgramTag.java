package org.academy.api.common.ability.program;

import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

/** A resource tag identifier that retains its leading {@code #} text representation. */
public record ProgramTag(ResourceLocation id) {
    public static final Codec<ProgramTag> CODEC = Codec.STRING.xmap(
            value -> new ProgramTag(ResourceLocation.parse(value.startsWith("#")
                    ? value.substring(1) : value)),
            value -> value.id().toString()
    );

    public ProgramTag {
        Objects.requireNonNull(id, "id");
    }

    @Override
    public String toString() {
        return "#" + id;
    }
}
