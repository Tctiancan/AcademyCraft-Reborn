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
import org.academy.internal.common.world.entity.skill.MagneticWeaponBlade;
import org.academy.internal.common.world.entity.skill.MagneticWeaponBladeMotion;

public final class MagneticWeaponBladeRenderer
        extends EntityRenderer<MagneticWeaponBlade> {
    public MagneticWeaponBladeRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.0f;
    }

    @Override
    public void render(MagneticWeaponBlade entity,
                       float entityYaw,
                       float partialTick,
                       PoseStack poseStack,
                       MultiBufferSource bufferSource,
                       int packedLight) {
        var weapon = entity.getWeapon();
        if (weapon.isEmpty()) return;

        var yRot = Mth.rotLerp(partialTick, entity.yRotO, entity.getYRot());
        var xRot = Mth.rotLerp(partialTick, entity.xRotO, entity.getXRot());
        var roll = MagneticWeaponBladeMotion.rollAt(entity.getAttackTick(), entity.getAttackSequence());

        poseStack.pushPose();
        poseStack.mulPose(Axis.YN.rotationDegrees(yRot));
        poseStack.mulPose(Axis.XP.rotationDegrees(xRot));
        poseStack.mulPose(Axis.ZP.rotationDegrees(roll));
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0f));
        poseStack.mulPose(Axis.ZP.rotationDegrees(-45.0f));
        poseStack.scale(1.45f, 1.45f, 1.45f);
        Minecraft.getInstance().getItemRenderer().renderStatic(
                weapon,
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
    public ResourceLocation getTextureLocation(MagneticWeaponBlade entity) {
        return MissingTextureAtlasSprite.getLocation();
    }
}
