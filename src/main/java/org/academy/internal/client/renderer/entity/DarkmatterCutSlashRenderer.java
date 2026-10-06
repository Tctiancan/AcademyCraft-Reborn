package org.academy.internal.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.academy.api.client.resources.R;
import org.academy.internal.common.world.entity.skill.DarkmatterCutSlash;
import org.joml.Matrix4f;

public final class DarkmatterCutSlashRenderer
        extends EntityRenderer<DarkmatterCutSlash> {
    private static final ResourceLocation[] FRAMES = {
            R.textures.darkmatter_cut_slash_effect_1,
            R.textures.darkmatter_cut_slash_effect_2,
            R.textures.darkmatter_cut_slash_effect_3,
            R.textures.darkmatter_cut_slash_effect_4
    };

    public DarkmatterCutSlashRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    private static void renderSlash(VertexConsumer consumer, Matrix4f matrix, int light,
                                    float alpha, float width, float height, boolean mirrored) {
        var halfWidth = width * 0.5f;
        var halfHeight = height * 0.5f;
        var topV = mirrored ? 1.0f : 0.0f;
        var bottomV = mirrored ? 0.0f : 1.0f;

        addVertex(consumer, matrix, -halfWidth, halfHeight, -halfWidth, 0, topV, alpha, light);
        addVertex(consumer, matrix, halfWidth, halfHeight, -halfWidth, 1, topV, alpha, light);
        addVertex(consumer, matrix, halfWidth, halfHeight, halfWidth, 1, bottomV, alpha, light);
        addVertex(consumer, matrix, -halfWidth, halfHeight, halfWidth, 0, bottomV, alpha, light);

        addVertex(consumer, matrix, -halfWidth, halfHeight, halfWidth, 0, bottomV, alpha, light);
        addVertex(consumer, matrix, halfWidth, halfHeight, halfWidth, 1, bottomV, alpha, light);
        addVertex(consumer, matrix, halfWidth, halfHeight, -halfWidth, 1, topV, alpha, light);
        addVertex(consumer, matrix, -halfWidth, halfHeight, -halfWidth, 0, topV, alpha, light);
    }

    private static void addVertex(VertexConsumer consumer, Matrix4f matrix,
                                  float x, float y, float z, float u, float v,
                                  float alpha, int light) {
        consumer.addVertex(matrix, x, y, z).setColor(1, 1, 1, alpha)
                .setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light).setNormal(0, 1, 0);
    }

    @Override
    public void render(DarkmatterCutSlash entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        var progress = Mth.clamp((entity.tickCount + partialTick) / entity.getDuration(), 0, 1);
        var swingProgress = Mth.clamp(progress * 1.7f, 0.0f, 1.0f);
        var life = Mth.sin(progress * Mth.PI);
        var alpha = life * 0.92f;
        if (alpha <= 0.001f) return;
        var frame = Mth.clamp((int) (progress * FRAMES.length), 0, FRAMES.length - 1);
        var scale = entity.getScale() * (0.92f + 0.18f * life);
        var width = 5.0f * scale * (0.82f + 0.18f * swingProgress);
        var height = 1.05f * entity.getScale();
        var mirrored = entity.getSwingDirection() < 0;

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(270 - entity.getYRot()));
        poseStack.mulPose(Axis.XP.rotationDegrees(-entity.getXRot()));
        var matrix = poseStack.last().pose();
        var consumer = bufferSource.getBuffer(RenderType.entityTranslucent(FRAMES[frame]));
        renderSlash(consumer, matrix, packedLight, alpha, width, height, mirrored);
        renderSlash(consumer, matrix, packedLight, alpha * 0.52f, width * 0.76f, height * 0.76f, !mirrored);
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(DarkmatterCutSlash entity) {
        return FRAMES[Mth.clamp((int) ((Mth.clamp((entity.tickCount + 0) / entity.getDuration(), 0, 1)) * FRAMES.length), 0, FRAMES.length - 1)];
    }
}
