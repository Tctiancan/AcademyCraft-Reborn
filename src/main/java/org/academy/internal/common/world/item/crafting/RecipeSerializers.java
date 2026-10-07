package org.academy.internal.common.world.item.crafting;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static org.academy.AcademyCraft.MOD_ID;

public final class RecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, MOD_ID);
    static final StreamCodec<RegistryFriendlyByteBuf, DarkmatterDuplicationRecipe>
            DARKMATTER_DUPLICATION_STREAM_CODEC = StreamCodec.of(
            (unused788, unused789) -> {
            },
            unused790 -> new DarkmatterDuplicationRecipe()
    );

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<DarkmatterDuplicationRecipe>>
            DARKMATTER_DUPLICATION = RECIPE_SERIALIZERS.register("darkmatter_duplication", () ->
            new RecipeSerializer<>() {
                @Override
                public MapCodec<DarkmatterDuplicationRecipe> codec() {
                    return MapCodec.unit(DarkmatterDuplicationRecipe::new);
                }

                @Override
                public StreamCodec<RegistryFriendlyByteBuf, DarkmatterDuplicationRecipe> streamCodec() {
                    return DARKMATTER_DUPLICATION_STREAM_CODEC;
                }
            });

    static final StreamCodec<RegistryFriendlyByteBuf, DarkmatterCoatingRecipe>
            DARKMATTER_COATING_STREAM_CODEC = StreamCodec.of(
            (unused791, unused792) -> {
            },
            unused793 -> new DarkmatterCoatingRecipe());

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<DarkmatterCoatingRecipe>>
            DARKMATTER_COATING = RECIPE_SERIALIZERS.register("darkmatter_coating", () ->
            new RecipeSerializer<>() {
                @Override
                public MapCodec<DarkmatterCoatingRecipe> codec() {
                    return MapCodec.unit(DarkmatterCoatingRecipe::new);
                }

                @Override
                public StreamCodec<RegistryFriendlyByteBuf, DarkmatterCoatingRecipe> streamCodec() {
                    return DARKMATTER_COATING_STREAM_CODEC;
                }
            });

    private RecipeSerializers() {
    }
}
