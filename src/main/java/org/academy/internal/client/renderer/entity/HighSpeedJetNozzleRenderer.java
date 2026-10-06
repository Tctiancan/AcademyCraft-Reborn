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
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.academy.internal.common.world.entity.skill.HighSpeedJetNozzle;

/** Renders the attached nozzle as a compact directional dropper head. */
public final class HighSpeedJetNozzleRenderer
        extends EntityRenderer<HighSpeedJetNozzle> {
    private ItemStack displayStack = ItemStack.EMPTY;

    public HighSpeedJetNozzleRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.0f;
    }

    @Override
    public void render(HighSpeedJetNozzle entity,
                       float entityYaw,
                       float partialTick,
                       PoseStack poseStack,
                       MultiBufferSource bufferSource,
                       int packedLight) {
        if (displayStack.isEmpty()) displayStack = Items.DROPPER.getDefaultInstance();
        var active = entity.activeTicks() > 0;
        poseStack.pushPose();
        poseStack.mulPose(Axis.YN.rotationDegrees(entity.getYRot()));
        poseStack.mulPose(Axis.XP.rotationDegrees(entity.getXRot()));
        poseStack.scale(active ? 0.34f : 0.3f,
                active ? 0.34f : 0.3f,
                active ? 0.34f : 0.3f);
        Minecraft.getInstance().getItemRenderer().renderStatic(
                displayStack,
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
    public ResourceLocation getTextureLocation(HighSpeedJetNozzle entity) {
        return MissingTextureAtlasSprite.getLocation();
    }
}
