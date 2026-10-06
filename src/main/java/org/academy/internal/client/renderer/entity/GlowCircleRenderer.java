package org.academy.internal.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.academy.api.client.render.Render;
import org.academy.api.client.render.post.PostEffect;
import org.academy.internal.common.ability.accelerator.skills.lv4.VectorReflection;
import org.academy.internal.common.world.entity.skill.GlowCircle;

public class GlowCircleRenderer extends EntityRenderer<GlowCircle> {
    private static final float MAX_RADIUS = 1.5f * 2.2f;

    public GlowCircleRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    public static float radiusAt(float p) {
        return MAX_RADIUS * Mth.sin(p * Mth.PI);
    }

    @Override
    public void render(GlowCircle entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        if (!isVisibleForCurrentCamera(entity.getEffectOwnerId())) return;
        var progress = Math.min((entity.ticks + partialTick) / GlowCircle.LIFE_TICKS, 1.0f);
        renderRing(poseStack, radiusAt(progress), entity.getXRot(), entity.getYRot());
    }

    public static void renderRing(PoseStack poseStack, float radius, float xRot, float yRot) {
        var distortionStrength = 0.025f;
        var ringWidth = 0.5f;
        var ringEdgeBlur = 0.05f;

        poseStack.pushPose();

        var matrix = poseStack.last().pose();
        var halfRadius = radius / 2f;

        poseStack.mulPose(Axis.YP.rotationDegrees(90 - yRot));
        poseStack.mulPose(Axis.ZP.rotationDegrees(90 + xRot));
        poseStack.mulPose(Axis.XP.rotationDegrees(180));

        var vertexConsumer = PostEffect.getPre().getBuffer(Render.RenderTypes.DISTORTION_RING);
        vertexConsumer.addVertex(matrix, -halfRadius, 0, -halfRadius).setUv(0, 0).setNormal(distortionStrength, ringWidth, ringEdgeBlur);
        vertexConsumer.addVertex(matrix, halfRadius, 0, -halfRadius).setUv(1, 0).setNormal(distortionStrength, ringWidth, ringEdgeBlur);
        vertexConsumer.addVertex(matrix, halfRadius, 0, halfRadius).setUv(1, 1).setNormal(distortionStrength, ringWidth, ringEdgeBlur);
        vertexConsumer.addVertex(matrix, -halfRadius, 0, halfRadius).setUv(0, 1).setNormal(distortionStrength, ringWidth, ringEdgeBlur);

        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(GlowCircle entity) {
        return MissingTextureAtlasSprite.getLocation();
    }

    public static boolean isVisibleForCurrentCamera(int ownerEntityId) {
        var minecraft = Minecraft.getInstance();
        var player = minecraft.player;
        if (player == null
                || !minecraft.options.getCameraType().isFirstPerson()
                || ownerEntityId != player.getId()) {
            return true;
        }
        return VectorReflection.Client.CONFIG.isFirstPersonDistortionRingVisible();
    }
}
