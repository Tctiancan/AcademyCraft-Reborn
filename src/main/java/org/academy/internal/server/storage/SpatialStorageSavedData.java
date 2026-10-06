package org.academy.internal.server.storage;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import org.academy.AcademyCraft;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.function.ToLongBiFunction;

/** Contents belong to the world, and survive transfers of the physical unit between players. */
public final class SpatialStorageSavedData extends SavedData {
    private static final SavedData.Factory<SpatialStorageSavedData> FACTORY = new SavedData.Factory<>(
            SpatialStorageSavedData::new, SpatialStorageSavedData::load, null);
    private final Map<UUID, LinkedHashMap<ItemStack, Long>> contents = new LinkedHashMap<>();
    private final Set<UUID> transferring = new HashSet<>();

    public SpatialStorageSavedData() {
    }

    public static SpatialStorageSavedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                FACTORY, AcademyCraft.academy("spatial_storage").toString());
    }

    public static SpatialStorageSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        var data = new SpatialStorageSavedData();
        var ops = RegistryOps.create(NbtOps.INSTANCE, registries);
        var contentsTag = tag.getList("contents", Tag.TAG_COMPOUND);
        for (var unitTagRaw : contentsTag) {
            var unitTag = (CompoundTag) unitTagRaw;
            var id = unitTag.getUUID("id");
            var items = new LinkedHashMap<ItemStack, Long>();
            var entriesTag = unitTag.getList("items", Tag.TAG_COMPOUND);
            for (var entryTagRaw : entriesTag) {
                var entryTag = (CompoundTag) entryTagRaw;
                var resource = ItemStack.OPTIONAL_CODEC.decode(ops, entryTag.get("resource"))
                        .result().map(Pair::getFirst).orElse(ItemStack.EMPTY);
                long count = entryTag.getLong("count");
                if (!resource.isEmpty() && count > 0) items.merge(resource, count, Math::addExact);
            }
            if (!items.isEmpty()) data.contents.put(id, items);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        var ops = RegistryOps.create(NbtOps.INSTANCE, registries);
        var contentsTag = new ListTag();
        for (var unit : contents.entrySet()) {
            var unitTag = new CompoundTag();
            unitTag.putUUID("id", unit.getKey());
            var entriesTag = new ListTag();
            for (var entry : unit.getValue().entrySet()) {
                var entryTag = new CompoundTag();
                ItemStack.OPTIONAL_CODEC.encodeStart(ops, entry.getKey())
                        .result().ifPresent(resource -> entryTag.put("resource", resource));
                entryTag.putLong("count", entry.getValue());
                entriesTag.add(entryTag);
            }
            unitTag.put("items", entriesTag);
            contentsTag.add(unitTag);
        }
        tag.put("contents", contentsTag);
        return tag;
    }

    public void store(UUID id, ItemStack stack) {
        if (stack.isEmpty()) return;
        contents.computeIfAbsent(id, ignored -> new LinkedHashMap<>())
                .merge(stack.copyWithCount(1), (long) stack.getCount(), Math::addExact);
        setDirty();
    }

    /** Consume a single matching item, used when a harvesting effect replants its crop. */
    public boolean consumeOne(UUID id, Predicate<ItemStack> predicate) {
        var items = contents.get(id);
        if (items == null || transferring.contains(id)) return false;
        var iterator = items.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            if (!predicate.test(entry.getKey())) continue;
            if (entry.getValue() == 1) iterator.remove();
            else entry.setValue(entry.getValue() - 1);
            setDirty();
            return true;
        }
        return false;
    }

    public long count(UUID id) {
        var items = contents.get(id);
        if (items == null) return 0;
        long total = 0;
        for (long count : items.values()) total = total > Long.MAX_VALUE - count ? Long.MAX_VALUE : total + count;
        return total;
    }

    /** The destination returns the accepted amount; refused contents stay in this unit. */
    public long transfer(UUID id, int maxTypes, ToLongBiFunction<ItemStack, Long> destination) {
        var items = contents.get(id);
        if (items == null || !transferring.add(id)) return 0;
        long total = 0;
        try {
            var entries = new ArrayList<>(items.entrySet());
            int visited = 0;
            for (var entry : entries) {
                if (visited++ >= maxTypes) break;
                long available = entry.getValue();
                long accepted = destination.applyAsLong(entry.getKey(), available);
                if (accepted < 0 || accepted > available) throw new IllegalStateException("Invalid accepted amount");
                // Rotate rejected entries so a capped automatic pass cannot starve later resources.
                long remaining = items.get(entry.getKey()) - accepted;
                items.remove(entry.getKey());
                if (remaining > 0) items.put(entry.getKey(), remaining);
                if (accepted > 0) {
                    setDirty();
                    total = total > Long.MAX_VALUE - accepted ? Long.MAX_VALUE : total + accepted;
                }
            }
            return total;
        } finally {
            transferring.remove(id);
        }
    }
}
