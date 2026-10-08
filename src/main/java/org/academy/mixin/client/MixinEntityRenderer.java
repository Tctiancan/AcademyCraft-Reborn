package org.academy.mixin.client;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.AABB;
import org.academy.internal.client.ability.mentalout.WideAreaInterferenceClientState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * 1.21.1 简化版 (C-stun)。
 *
 * 26.2 通过 {@code extractRenderState} 写回 {@code EntityRenderState.outlineColor} 给实体会员描边
 * (高速喷流 / 广域干扰)。1.21.1 无渲染状态 (EntityRenderState) 且 {@code EntityRenderer} 无
 * {@code extractRenderState} 钩子，此处仅保留可移植的 RTS 实体距离判定 {@link #academy$measureRtsEntityDistanceFromFocus}。
 */
@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer {
    @WrapOperation(method = "shouldRender", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;shouldRender(DDD)Z"))
    private boolean academy$measureRtsEntityDistanceFromFocus(Entity entity, double x, double y, double z,
            Operation<Boolean> original) {
        if (WideAreaInterferenceClientState.isRtsView()) {
            var focus = WideAreaInterferenceClientState.focusPosition();
            var range = Math.max(128, Minecraft.getInstance().options.renderDistance().get() * 16);
            return new AABB(focus, focus).inflate(range).intersects(entity.getBoundingBox());
        }
        return original.call(entity, x, y, z);
    }
}
