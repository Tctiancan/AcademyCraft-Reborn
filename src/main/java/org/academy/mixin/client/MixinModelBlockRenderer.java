package org.academy.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.academy.internal.client.ability.mentalout.WideAreaInterferenceClientState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelBlockRenderer.class)
public abstract class MixinModelBlockRenderer {
    // 26.2 签名为 tesselateBlock(BlockQuadOutput, float offsetX, float offsetY, float offsetZ, ...),
    // 1.21.1 为 (BlockAndTintGetter, BakedModel, BlockState, BlockPos, PoseStack, VertexConsumer,
    //          boolean, RandomSource, long, int, ModelData, RenderType)。
    // 目标 neoforge 12 参重载(两条重载路径均委托于此),仅需 BlockPos 判断隐藏。
    @Inject(method = "tesselateBlock(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/client/resources/model/BakedModel;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZLnet/minecraft/util/RandomSource;JILnet/neoforged/neoforge/client/model/data/ModelData;Lnet/minecraft/client/renderer/RenderType;)V", at = @At("HEAD"), cancellable = true)
    private void academy$hideWideAreaBlock(
            BlockAndTintGetter level,
            BakedModel model,
            BlockState state,
            BlockPos pos,
            PoseStack poseStack,
            VertexConsumer consumer,
            boolean checkSides,
            RandomSource random,
            long seed,
            int packedOverlay,
            ModelData modelData,
            RenderType renderType,
            CallbackInfo ci
    ) {
        if (WideAreaInterferenceClientState.isBlockHidden(pos)) ci.cancel();
    }
}
