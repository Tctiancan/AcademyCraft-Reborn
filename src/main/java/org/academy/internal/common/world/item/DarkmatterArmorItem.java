package org.academy.internal.common.world.item;

import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import org.academy.api.common.ability.darkmatter.DarkmatterShape;

/**
 * Native dark matter armor pieces. Structural integrity never breaks the stack.
 */
public class DarkmatterArmorItem extends ArmorItem implements DarkmatterShapedItem {
    public DarkmatterArmorItem(Holder<ArmorMaterial> material, ArmorItem.Type type,
                               Properties properties) {
        super(material, type, DarkmatterNativeItemSupport.equipmentProperties(properties));
    }

    @Override
    public DarkmatterShape darkmatterShape() {
        return DarkmatterShape.ARMOR;
    }

    @Override
    public float getXpRepairRatio(ItemStack stack) {
        return 0.0f;
    }

    @Override
    public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
        return !enchantment.is(Enchantments.MENDING)
                && super.supportsEnchantment(stack, enchantment);
    }

    @Override
    public boolean canGrindstoneRepair(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return DarkmatterNativeItemSupport.isBarVisible(stack);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return DarkmatterNativeItemSupport.barWidth(stack);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return DarkmatterNativeItemSupport.barColor(stack);
    }

    @Override
    public boolean shouldCauseReequipAnimation(
            ItemStack oldStack, ItemStack newStack, boolean slotChanged
    ) {
        return DarkmatterNativeItemSupport.shouldCauseReequipAnimation(
                oldStack, newStack, slotChanged);
    }

    @Override
    public boolean shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack) {
        return DarkmatterNativeItemSupport.shouldCauseBlockBreakReset(oldStack, newStack);
    }
}
