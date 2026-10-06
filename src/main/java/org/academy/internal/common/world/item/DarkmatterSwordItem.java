package org.academy.internal.common.world.item;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import org.academy.api.common.ability.darkmatter.DarkmatterShape;

public final class DarkmatterSwordItem extends DarkmatterEquipmentItem {
    public DarkmatterSwordItem(Properties properties) {
        super(properties);
    }

    @Override
    public DarkmatterShape darkmatterShape() {
        return DarkmatterShape.SWORD;
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility action) {
        if (!DarkmatterItemUtil.isOperational(stack)) return false;
        return action == ItemAbilities.SWORD_SWEEP
                || super.canPerformAction(stack, action);
    }
}
