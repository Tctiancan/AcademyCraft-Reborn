package org.academy.internal.client.renderer.special;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * C-stun: 26.2 {@code SpecialModelRenderer<T>} 专属特殊物品渲染在 1.21.1 中暂不移植。
 * 保留对外静态入口方法签名，方法体为空占位。
 */
public final class AbilityControlTabletSpecialRenderer {
    public static final AbilityControlTabletSpecialRenderer INSTANCE = new AbilityControlTabletSpecialRenderer();

    private AbilityControlTabletSpecialRenderer() {
    }

    public static void tickHeldItems() {
        // C-stun: 手持动画状态更新暂不移植。
    }

    public static void prepareItemRender(
            LivingEntity entity,
            ItemStack stack,
            ItemDisplayContext displayContext
    ) {
        // C-stun: 特殊物品渲染参数准备暂不移植。
    }

    public static void prepareThirdPersonRender(LivingEntity entity) {
        // C-stun: 第三人称特殊物品渲染参数准备暂不移植。
    }
}
