package org.academy.mixin.client;

import net.minecraft.client.renderer.entity.ThrownTridentRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.ThrownTrident;
import org.academy.internal.common.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static org.academy.AcademyCraft.academy;

/**
 * 让原版的硬编码三叉戟渲染器感知物品(黑暗物质三叉戟使用专属实体纹理)。
 * 26.2 经 render-state + ContextKey 判断;1.21.1 三叉戟直接渲染,纹理来自
 * getTextureLocation(entity),在此注入替换纹理即可。
 */
@Mixin(ThrownTridentRenderer.class)
public abstract class MixinThrownTridentRenderer {
    @Inject(method = "getTextureLocation", at = @At("HEAD"), cancellable = true)
    private void academy$useDarkmatterTexture(ThrownTrident entity, CallbackInfoReturnable<ResourceLocation> cir) {
        if (entity.getWeaponItem().is(Items.DARKMATTER_TRIDENT.get())) {
            cir.setReturnValue(academy("textures/entity/darkmatter_trident.png"));
        }
    }
}
