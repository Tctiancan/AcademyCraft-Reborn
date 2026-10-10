package org.academy.internal.client.renderer.effect;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.academy.api.client.renderer.EffectRenderer;
import org.academy.api.client.resources.R;
import org.academy.internal.common.attachment.AttachmentTypes;
import org.joml.Matrix4f;

import static org.academy.AcademyCraft.academy;

public final class LightShieldEffectRenderer implements EffectRenderer {
    public static final ResourceLocation CONTEXT_KEY = academy("light_shield");
    public static final LightShieldEffectRenderer INSTANCE = new LightShieldEffectRenderer();
    private static final float THIRD_PERSON_HALF_SIZE = 1.55f;
    private static final float FIRST_PERSON_HALF_SIZE = 2.25f;

    private LightShieldEffectRenderer() {
    }

    private static void submitShield(PoseStack poseStack, MultiBufferSource bufferSource,
                                     int packedLight, float halfSize) {
        var consumer = bufferSource.getBuffer(
                RenderType.entityTranslucent(R.textures.light_shield_effect));
        addDoubleSidedQuad(consumer, poseStack.last().pose(), packedLight, halfSize);
    }

    private static void addDoubleSidedQuad(VertexConsumer consumer, Matrix4f matrix,
                                           int packedLight, float halfSize) {
        addQuad(consumer, matrix, packedLight, halfSize, false);
        addQuad(consumer, matrix, packedLight, halfSize, true);
    }

    private static void addQuad(VertexConsumer consumer, Matrix4f matrix, int packedLight,
                                float halfSize, boolean reverse) {
        if (reverse) {
            vertex(consumer, matrix, halfSize, -halfSize, 1, 0, packedLight);
            vertex(consumer, matrix, -halfSize, -halfSize, 0, 0, packedLight);
            vertex(consumer, matrix, -halfSize, halfSize, 0, 1, packedLight);
            vertex(consumer, matrix, halfSize, halfSize, 1, 1, packedLight);
        } else {
            vertex(consumer, matrix, -halfSize, -halfSize, 0, 0, packedLight);
            vertex(consumer, matrix, halfSize, -halfSize, 1, 0, packedLight);
            vertex(consumer, matrix, halfSize, halfSize, 1, 1, packedLight);
            vertex(consumer, matrix, -halfSize, halfSize, 0, 1, packedLight);
        }
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix, float x, float y,
                               float u, float v, int packedLight) {
        consumer.addVertex(matrix, x, y, 0)
                .setColor(1.0f, 1.0f, 1.0f, 0.85f)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(packedLight)
                .setNormal(0, 0, 1);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                       float yRot, float xRot) {
        var player = Minecraft.getInstance().player;
        if (player == null || !player.getData(AttachmentTypes.LIGHT_SHIELD_ACTIVE.get())) return;
        poseStack.pushPose();
        poseStack.translate(0, -0.25, -1.4);
        poseStack.mulPose(Axis.XP.rotationDegrees(-xRot));
        poseStack.mulPose(Axis.ZP.rotationDegrees(player.tickCount * 12.0f));
        submitShield(poseStack, bufferSource, packedLight, THIRD_PERSON_HALF_SIZE);
        poseStack.popPose();
    }

    @Override
    public void renderFirstPerson(PoseStack poseStack, MultiBufferSource bufferSource,
                                  LocalPlayer player, int packedLight, float partialTick) {
        if (!player.getData(AttachmentTypes.LIGHT_SHIELD_ACTIVE.get())) return;
        poseStack.pushPose();
        poseStack.translate(0, 0, -1.5);
        poseStack.mulPose(Axis.ZP.rotationDegrees((player.tickCount + partialTick) * 12.0f));
        submitShield(poseStack, bufferSource, packedLight, FIRST_PERSON_HALF_SIZE);
        poseStack.popPose();
    }
}
