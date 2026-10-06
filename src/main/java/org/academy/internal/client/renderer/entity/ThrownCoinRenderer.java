package org.academy.internal.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import org.academy.internal.common.world.entity.projectile.ThrownCoin;

public class ThrownCoinRenderer extends EntityRenderer<ThrownCoin> {
    public ThrownCoinRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(ThrownCoin entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YN.rotationDegrees(entity.getYRot()));
        poseStack.mulPose(Axis.XP.rotationDegrees(Mth.lerp(partialTick, entity.angleOld, entity.angle) * 50));
        poseStack.translate(0, -0.125f, 0);
        Minecraft.getInstance().getItemRenderer().renderStatic(
                entity.getItem(),
                ItemDisplayContext.FIXED,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                bufferSource,
                entity.level(),
                entity.getId()
        );
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(ThrownCoin entity) {
        return MissingTextureAtlasSprite.getLocation();
    }
}
