package org.academy.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.academy.internal.client.animation.AbilityDeveloperSleepClient;
import org.academy.internal.client.animation.WingFlightAnimationClient;
import org.academy.internal.client.render.vfx.WingAvatarRegistry;
import org.academy.internal.common.attachment.AttachmentTypes;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(PlayerRenderer.class)
public abstract class MixinAvatarRenderer {
    @Unique
    private ItemStack academy$hiddenHandItem = ItemStack.EMPTY;

    /**
     * 1.21.1 适配：26.2 的 setupRotations(PlayerRenderState,...) 依赖不存在的 PlayerRenderState。
     * 1.21.1 PlayerRenderer.setupRotations(AbstractClientPlayer, PoseStack, F,F,F,F,F) 以实体传入，
     * 此处直接用实体做舱体对齐。头部钩子顺带抑制了 26.2 版 state-based 姿态提取。
     */
    @Inject(method = "setupRotations(Lnet/minecraft/client/player/AbstractClientPlayer;Lcom/mojang/blaze3d/vertex/PoseStack;FFFF)V", at = @At("HEAD"))
    private void alignWithAbilityDeveloperPod(
            AbstractClientPlayer entity,
            PoseStack poseStack,
            float bob,
            float yBodyRot,
            float partialTick,
            float scale,
            CallbackInfo ci
    ) {
        AbilityDeveloperSleepClient.applyModelTransform(entity, poseStack, partialTick);
    }

    @Inject(method = "setupRotations(Lnet/minecraft/client/player/AbstractClientPlayer;Lcom/mojang/blaze3d/vertex/PoseStack;FFFF)V", at = @At("RETURN"))
    private void captureModelRootMatrix(
            AbstractClientPlayer entity,
            PoseStack poseStack,
            float bob,
            float yBodyRot,
            float partialTick,
            float scale,
            CallbackInfo ci
    ) {
        var matrix = new Matrix4f(poseStack.last().pose());
        matrix.scale(-1.0f, -1.0f, 1.0f);
        matrix.scale(0.9375f);
        matrix.translate(0.0f, -1.501f, 0.0f);
        WingAvatarRegistry.capture(entity.getId(), matrix);
    }

    @Inject(method = "render(Lnet/minecraft/client/player/AbstractClientPlayer;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("HEAD"))
    private void hideMagneticWeaponInMainHand(
            AbstractClientPlayer entity,
            float entityYaw,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            CallbackInfo ci
    ) {
        WingFlightAnimationClient.prepareRenderState(entity, partialTicks);
        var data = entity.getData(AttachmentTypes.MAGNETIC_WEAPON_DATA.get());
        if (!data.active() || !data.hideMainHand()) return;
        // 1.21.1 无 26.2 的 renderHandItem(state.*) 提取；改为渲染期临时置空主/副手物品，
        // RETURN 时恢复，隐藏磁性武器在手中呈现。
        var hideRight = entity.getMainArm() == HumanoidArm.RIGHT;
        academy$hiddenHandItem = hideRight
                ? entity.getMainHandItem()
                : entity.getItemInHand(InteractionHand.OFF_HAND);
        entity.setItemInHand(
                hideRight ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND,
                ItemStack.EMPTY
        );
    }

    @Inject(method = "render(Lnet/minecraft/client/player/AbstractClientPlayer;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("RETURN"))
    private void restoreMagneticWeaponInMainHand(
            AbstractClientPlayer entity,
            float entityYaw,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            CallbackInfo ci
    ) {
        if (academy$hiddenHandItem.isEmpty()) return;
        entity.setItemInHand(
                entity.getMainArm() == HumanoidArm.RIGHT ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND,
                academy$hiddenHandItem
        );
        academy$hiddenHandItem = ItemStack.EMPTY;
    }
}
