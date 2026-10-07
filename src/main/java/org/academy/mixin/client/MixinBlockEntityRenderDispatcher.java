package org.academy.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.academy.internal.client.ability.mentalout.WideAreaInterferenceClientState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntityRenderDispatcher.class)
public abstract class MixinBlockEntityRenderDispatcher {
    // 26.2 经 tryExtractRenderState(返回 render-state)在节点化提交期隐藏方块实体;
    // 1.21.1 方块实体直接 render(E, float, PoseStack, MultiBufferSource),在此取消即可。
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void academy$hideWideAreaBlockEntity(
            BlockEntity blockEntity,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            CallbackInfo ci
    ) {
        if (WideAreaInterferenceClientState.isBlockHidden(blockEntity.getBlockPos())) {
            ci.cancel();
        }
    }
}
