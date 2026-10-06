package org.academy.internal.common.compat;

import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;

import java.util.Optional;
import java.util.function.Supplier;

/** Shared by WorldWeaver serialization and BetterEnd's direct world-config accesses. */
public final class WorldWeaverConfigAccess {
    private static final Object LOCK = new Object();

    private WorldWeaverConfigAccess() {}

    public static boolean contains(CompoundTag tag, String key) {
        return locked(() -> tag.contains(key));
    }

    public static int getIntOr(CompoundTag tag, String key, int fallback) {
        return locked(() -> tag.contains(key, Tag.TAG_INT) ? tag.getInt(key) : fallback);
    }

    public static void putInt(CompoundTag tag, String key, int value) {
        locked(() -> {
            tag.putInt(key, value);
            return null;
        });
    }

    public static <T> Optional<T> read(CompoundTag tag, String key, Codec<T> codec) {
        return locked(() -> NbtOps.INSTANCE.getTag(tag, key)
                .flatMap(element -> codec.parse(NbtOps.INSTANCE, element).result()));
    }

    public static <T> void store(CompoundTag tag, String key, Codec<T> codec, T value) {
        locked(() -> {
            codec.encodeStart(NbtOps.INSTANCE, value)
                    .result()
                    .ifPresent(element -> tag.put(key, element));
            return null;
        });
    }

    /** Never hold this lock while requesting or generating a chunk. */
    public static <T> T locked(Supplier<T> action) {
        synchronized (LOCK) {
            return action.get();
        }
    }
}
