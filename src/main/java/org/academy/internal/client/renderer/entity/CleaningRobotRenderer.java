package org.academy.internal.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.academy.api.client.resources.R;
import org.academy.internal.client.model.CleaningRobotModel;
import org.academy.internal.common.world.entity.vehicle.CleaningRobot;

public class CleaningRobotRenderer extends EntityRenderer<CleaningRobot> {
    public static final CleaningRobotModel CLEANING_ROBOT_MODEL = new CleaningRobotModel(CleaningRobotModel.createBodyLayer().bakeRoot());

    protected CleaningRobotRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(CleaningRobot entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        poseStack.rotateAround(Axis.XP.rotationDegrees(180), 0, 0.75f, 0);
        float scale = 1.0F;
        poseStack.scale(scale, scale, scale);
        poseStack.translate(0.0F, 1.5F, 0.0F);
        CLEANING_ROBOT_MODEL.setupAnim(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        var vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(R.textures.model.cleaning_robot));
        CLEANING_ROBOT_MODEL.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(CleaningRobot entity) {
        return R.textures.model.cleaning_robot;
    }
}
