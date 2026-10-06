package org.academy.internal.common.world.item;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import org.academy.AcademyCraft;

import java.util.EnumMap;
import java.util.List;
import java.util.function.Supplier;

public final class DarkmatterArmorMaterial {
    public static final TagKey<Item> REPAIR_INGREDIENT = TagKey.create(
            Registries.ITEM, AcademyCraft.academy("repairs_dark_matter_armor"));
    public static final Holder<ArmorMaterial> INSTANCE = Holder.direct(create());

    private DarkmatterArmorMaterial() {
    }

    private static ArmorMaterial create() {
        var defense = new EnumMap<ArmorItem.Type, Integer>(ArmorItem.Type.class);
        defense.put(ArmorItem.Type.HELMET, 3);
        defense.put(ArmorItem.Type.CHESTPLATE, 8);
        defense.put(ArmorItem.Type.LEGGINGS, 6);
        defense.put(ArmorItem.Type.BOOTS, 3);
        defense.put(ArmorItem.Type.BODY, 8);
        Supplier<Ingredient> repairIngredient = () -> Ingredient.of(REPAIR_INGREDIENT);
        List<ArmorMaterial.Layer> layers = List.of(
                new ArmorMaterial.Layer(AcademyCraft.academy("dark_matter")));
        return new ArmorMaterial(
                defense,
                25,
                SoundEvents.ARMOR_EQUIP_NETHERITE,
                repairIngredient,
                layers,
                3.0f,
                0.1f
        );
    }
}
