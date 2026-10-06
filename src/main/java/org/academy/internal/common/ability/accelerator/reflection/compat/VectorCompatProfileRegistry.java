package org.academy.internal.common.ability.accelerator.reflection.compat;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.damagesource.DamageSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import org.academy.AcademyCraft;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@EventBusSubscriber(modid = AcademyCraft.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class VectorCompatProfileRegistry extends SimpleJsonResourceReloadListener {
    private static final VectorCompatProfileRegistry INSTANCE = new VectorCompatProfileRegistry();
    private static volatile List<ProfileEntry> profiles = List.of();
    private static volatile VectorCompatibilityMode mode = VectorCompatibilityMode.SAFE;

    private VectorCompatProfileRegistry() {
        super(new Gson(), "academy_vector_compat");
    }

    @SubscribeEvent
    public static void registerReloadListener(AddReloadListenerEvent event) {
        event.addListener(INSTANCE);
    }

    public static Optional<ProfileEntry> find(DamageSource source) {
        return profiles.stream()
                .filter(entry -> entry.profile().matches(source))
                .findFirst();
    }

    public static List<ProfileEntry> profiles() {
        return profiles;
    }

    public static VectorCompatibilityMode mode() {
        return mode;
    }

    public static void setMode(VectorCompatibilityMode newMode) {
        mode = newMode == null ? VectorCompatibilityMode.SAFE : newMode;
    }

    @Override
    protected void apply(
            Map<ResourceLocation, JsonElement> prepared,
            ResourceManager resourceManager,
            ProfilerFiller profiler
    ) {
        profiles = prepared.entrySet().stream()
                .map(entry -> VectorCompatProfile.CODEC
                        .parse(JsonOps.INSTANCE, entry.getValue())
                        .resultOrPartial(error -> AcademyCraft.LOGGER.error(
                                "Failed to parse vector compatibility profile {}: {}",
                                entry.getKey(), error))
                        .map(profile -> new ProfileEntry(entry.getKey(), profile)))
                .flatMap(Optional::stream)
                .sorted(Comparator
                        .<ProfileEntry>comparingInt(entry -> entry.profile().deny() ? 1 : 0)
                        .reversed()
                        .thenComparing(
                                Comparator.comparingInt((ProfileEntry entry) -> entry.profile().priority())
                                        .reversed()
                        ))
                .toList();
        AcademyCraft.LOGGER.info("Loaded {} vector compatibility profiles", profiles.size());
    }

    public record ProfileEntry(ResourceLocation id, VectorCompatProfile profile) {
    }
}
