package org.academy.internal.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.academy.api.client.render.Render;
import org.academy.api.client.render.post.PostEffect;
import org.academy.internal.common.world.entity.skill.KineticShockwave;

public final class KineticShockwaveRenderer
        extends EntityRenderer<KineticShockwave> {
    public KineticShockwaveRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    private static void emitRing(PoseStack poseStack, float radius, float strength,
                                 float width, float blur, float xRot, float yRot, float zRot) {
        poseStack.pushPose();
        if (yRot != 0.0f) poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
        if (zRot != 0.0f) poseStack.mulPose(Axis.ZP.rotationDegrees(zRot));
        if (xRot != 0.0f) poseStack.mulPose(Axis.XP.rotationDegrees(xRot));
        var matrix = poseStack.last().pose();
        var consumer = PostEffect.getPre().getBuffer(Render.RenderTypes.DISTORTION_RING);
        // 当前后处理阶段缓冲为 POSITION_COLOR, 需显式填充颜色元素, 否则 addVertex 校验 "Missing elements in vertex: Color"。
        consumer.addVertex(matrix, -radius, 0, -radius).setColor(0xFFFFFFFF).setUv(0, 0).setNormal(strength, width, blur);
        consumer.addVertex(matrix, radius, 0, -radius).setColor(0xFFFFFFFF).setUv(1, 0).setNormal(strength, width, blur);
        consumer.addVertex(matrix, radius, 0, radius).setColor(0xFFFFFFFF).setUv(1, 1).setNormal(strength, width, blur);
        consumer.addVertex(matrix, -radius, 0, radius).setColor(0xFFFFFFFF).setUv(0, 1).setNormal(strength, width, blur);
        poseStack.popPose();
    }

    public static void renderRings(PoseStack poseStack, float radius, float progress,
                                   float intensity, float xRot, float yRot) {
        if (radius <= 0.01f || progress >= 1.0f) return;
        var fade = 1.0f - progress;
        var strength = Mth.clamp(0.018f + intensity * 0.006f, 0.02f, 0.055f) * fade;
        var width = Mth.clamp(0.18f + intensity * 0.035f, 0.2f, 0.42f);
        var blur = 0.055f + progress * 0.04f;

        emitRing(poseStack, radius, strength, width, blur, 0.0f, 0.0f, 0.0f);
        emitRing(poseStack, radius, strength * 0.8f, width, blur, 90.0f, 0.0f, 0.0f);
        emitRing(poseStack, radius, strength * 0.8f, width, blur, 0.0f, 0.0f, 90.0f);
        emitRing(poseStack, radius, strength, width * 0.8f, blur,
                180.0f, 90.0f - yRot, 90.0f + xRot);
    }

    @Override
    public void render(KineticShockwave entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        var progress = Mth.clamp((entity.tickCount + partialTick) / entity.getLifeTicks(), 0.0f, 1.0f);
        var radius = entity.getMaxRadius() * progress;
        renderRings(poseStack, radius, progress, entity.getIntensity(), entity.getXRot(), entity.getYRot());
    }

    @Override
    public ResourceLocation getTextureLocation(KineticShockwave entity) {
        return MissingTextureAtlasSprite.getLocation();
    }
}
