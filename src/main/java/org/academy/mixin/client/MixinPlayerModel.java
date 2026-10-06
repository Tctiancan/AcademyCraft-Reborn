package org.academy.mixin.client;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import org.academy.internal.client.render.vfx.WingAvatarRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerModel.class)
public abstract class MixinPlayerModel {
    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/PlayerRenderState;)V", at = @At("RETURN"))
    private void captureHands(PlayerRenderState state, CallbackInfo ci) {
        WingAvatarRegistry.captureHands(state, (PlayerModel) (Object) this);
    }
}
