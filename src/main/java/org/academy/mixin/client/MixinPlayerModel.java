package org.academy.mixin.client;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.Entity;
import org.academy.internal.client.render.vfx.WingAvatarRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerModel.class)
public abstract class MixinPlayerModel {
    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/Entity;FFFFFF)V", at = @At("RETURN"))
    private void captureHands(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        WingAvatarRegistry.captureHands(entity.getId(), (PlayerModel) (Object) this);
    }
}
