package org.academy.internal.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import org.academy.api.client.render.Render;
import org.academy.internal.client.renderer.blockentity.state.CatEngineRenderState;
import org.academy.internal.common.world.level.block.entity.CatEngineBlockEntity;
import org.joml.Vector3f;

public final class CatEngineRenderer implements BlockEntityRenderer<CatEngineBlockEntity> {
    public static final CatEngineRenderer INSTANCE = new CatEngineRenderer();

    private CatEngineRenderer() {
    }

    @Override
    public void render(CatEngineBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        var renderState = new CatEngineRenderState();
        renderState.enable = blockEntity.enable;
        renderState.oRot = blockEntity.oRot;
        renderState.partialTick = partialTick;
        renderState.rH = blockEntity.rH;
        renderState.rot = blockEntity.rot;

        poseStack.pushPose();
        var sizeHalf = 0.5f;
        var f1 = renderState.rot - renderState.oRot;

        f1 %= Mth.TWO_PI;

        if (f1 >= Mth.PI) {
            f1 -= Mth.TWO_PI;
        }
        if (f1 < -Mth.PI) {
            f1 += Mth.TWO_PI;
        }
        var f2 = renderState.oRot + f1 * renderState.partialTick;
        poseStack.rotateAround(Axis.YN.rotation(f2), 0.5f, 0.5f, 0.5f);
        poseStack.rotateAround(Axis.YN.rotation(90), 0.5f, 0.5f, 0.5f);
        if (renderState.enable) {
            poseStack.rotateAround(Axis.XN.rotation(renderState.rH += 0.2F), 0.5f, 0.5f, 0.5f);
        }
        poseStack.translate(sizeHalf, sizeHalf, sizeHalf);
        poseStack.mulPose(Axis.XP.rotationDegrees(90));

        var vertexConsumer = bufferSource.getBuffer(Render.RenderTypes.CAT_ENGINE);
        var matrix = poseStack.last().pose();
        var normalMatrix = poseStack.last().normal();
        var normal = normalMatrix.transform(0, 1, 0, new Vector3f());

        vertexConsumer.addVertex(matrix, -sizeHalf, 0, -sizeHalf).setColor(255, 255, 255, 255).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normal.x(), normal.y(), normal.z());
        vertexConsumer.addVertex(matrix, sizeHalf, 0, -sizeHalf).setColor(255, 255, 255, 255).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normal.x(), normal.y(), normal.z());
        vertexConsumer.addVertex(matrix, sizeHalf, 0, sizeHalf).setColor(255, 255, 255, 255).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normal.x(), normal.y(), normal.z());
        vertexConsumer.addVertex(matrix, -sizeHalf, 0, sizeHalf).setColor(255, 255, 255, 255).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(normal.x(), normal.y(), normal.z());
        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(CatEngineBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return Minecraft.getInstance().options.getEffectiveRenderDistance() * 16;
    }
}
