package org.academy.internal.common.ability.accelerator.reflection.compat;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import org.academy.AcademyCraft;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@EventBusSubscriber(modid = AcademyCraft.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class VectorProjectileCompatRegistry extends
        SimpleJsonResourceReloadListener {
    private static final VectorProjectileCompatRegistry INSTANCE =
            new VectorProjectileCompatRegistry();
    private static volatile Set<ResourceLocation> ownerlessPeerCollisionSuppressedTypes = Set.of();

    private VectorProjectileCompatRegistry() {
        super(new Gson(), "academy_vector_projectile_compat");
    }

    @SubscribeEvent
    public static void registerReloadListener(AddReloadListenerEvent event) {
        event.addListener(INSTANCE);
    }

    public static boolean suppressesOwnerlessPeerCollision(EntityType<?> type) {
        return type != null && ownerlessPeerCollisionSuppressedTypes.contains(
                BuiltInRegistries.ENTITY_TYPE.getKey(type));
    }

    @Override
    protected void apply(
            Map<ResourceLocation, JsonElement> prepared,
            ResourceManager resourceManager,
            ProfilerFiller profiler
    ) {
        ownerlessPeerCollisionSuppressedTypes = prepared.values().stream()
                .map(json -> VectorProjectileCompatProfile.CODEC
                        .parse(JsonOps.INSTANCE, json)
                        .resultOrPartial(error -> AcademyCraft.LOGGER.error(
                                "Failed to parse vector projectile compatibility profile: {}", error)))
                .flatMap(Optional::stream)
                .filter(VectorProjectileCompatProfile::suppressOwnerlessPeerCollision)
                .flatMap(profile -> profile.entityTypes().stream())
                .collect(Collectors.toUnmodifiableSet());
        AcademyCraft.LOGGER.info(
                "Loaded {} vector projectile compatibility collision guards",
                ownerlessPeerCollisionSuppressedTypes.size());
    }
}
