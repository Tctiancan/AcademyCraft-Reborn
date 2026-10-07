package org.academy.mixin.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.block.LiquidBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.academy.internal.client.ability.mentalout.WideAreaInterferenceClientState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LiquidBlockRenderer.class)
public abstract class MixinFluidRenderer {
    // 26.2 为 FluidRenderer，1.21.1 为 LiquidBlockRenderer；签名 tesselate(BlockAndTintGetter,
    // BlockPos, VertexConsumer, BlockState, FluidState)。兼容原 WideAreaInterference 隐藏流体逻辑。
    @Inject(method = "tesselate", at = @At("HEAD"), cancellable = true)
    private void academy$hideWideAreaFluid(
            BlockAndTintGetter level,
            BlockPos pos,
            VertexConsumer buffer,
            BlockState blockState,
            FluidState fluidState,
            CallbackInfo ci
    ) {
        if (WideAreaInterferenceClientState.isBlockHidden(pos)) ci.cancel();
    }
}
