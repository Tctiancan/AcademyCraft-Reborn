package org.academy.internal.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import org.academy.api.client.render.Render;
import org.academy.api.client.render.post.GlowEffect;
import org.academy.api.client.renderer.BallRenderer;
import org.academy.api.client.util.VertexUtil;
import org.academy.internal.common.world.entity.skill.LightOrb;

public class LightOrbRenderer extends EntityRenderer<LightOrb> {
    public static final float[][] HEAD_BUFFER = VertexUtil.Ball.getIcosphereVertexBuffer(1, 2, true);
    private static final int FULL_BRIGHT = 15728880;
    private final RandomSource random = RandomSource.create();

    public LightOrbRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(LightOrb entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        var scale = entity.getScale();

        var r = 0.2f;
        var g = 0.8f;
        var b = 1.0f;

        var additiveBuilder = GlowEffect.getAfter().getBuffer(Render.RenderTypes.POS_COLOR_TRANGLES_GLOW_ADDITIVE);

        random.setSeed(System.currentTimeMillis() / 40);
        var cameraRotation = entityRenderDispatcher.cameraOrientation();

        poseStack.pushPose();
        poseStack.scale(scale, scale, scale);

        // 1.21.1 即时渲染: 替代 26.2 nodeCollector.submitCustomGeometry
        var vertexConsumer = bufferSource.getBuffer(Render.RenderTypes.POS_COLOR_TRANGLES);
        poseStack.pushPose();
        poseStack.mulPose(cameraRotation);
        poseStack.scale(0.35f, 0.35f, 0.35f);
        BallRenderer.renderBall(
                poseStack.last(),
                vertexConsumer,
                HEAD_BUFFER,
                1.0f, 1.0f, 1.0f, 1.0f,
                FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY
        );
        poseStack.popPose();

        // 内层辉光
        poseStack.pushPose();
        poseStack.mulPose(cameraRotation);
        var pulse = 1.0f + random.nextFloat() * 0.1f;
        poseStack.scale(0.7f * pulse, 0.7f * pulse, 0.7f * pulse);
        BallRenderer.renderBall(
                poseStack.last(), additiveBuilder, HEAD_BUFFER,
                r, g, b, 0.6f,
                FULL_BRIGHT, OverlayTexture.NO_OVERLAY
        );
        poseStack.popPose();

        // 中层辉光
        poseStack.pushPose();
        poseStack.mulPose(cameraRotation);
        poseStack.scale(1.5f, 1.5f, 1.5f);
        BallRenderer.renderBall(
                poseStack.last(), additiveBuilder, HEAD_BUFFER,
                r, g, b, 0.3f,
                FULL_BRIGHT, OverlayTexture.NO_OVERLAY
        );
        poseStack.popPose();

        // 外层辉光
        poseStack.pushPose();
        poseStack.mulPose(cameraRotation);
        var outerScale = 3.0f + random.nextFloat() * 0.5f;
        poseStack.scale(outerScale, outerScale, outerScale);
        BallRenderer.renderBall(
                poseStack.last(), additiveBuilder, HEAD_BUFFER,
                r, g, b, 0.1f, // Alpha 0.1
                FULL_BRIGHT, OverlayTexture.NO_OVERLAY
        );
        poseStack.popPose();

        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(LightOrb entity) {
        return MissingTextureAtlasSprite.getLocation();
    }
}
