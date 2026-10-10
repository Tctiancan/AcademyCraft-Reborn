package org.academy.internal.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.academy.api.client.renderer.CylinderRenderer;
import org.academy.api.client.util.VertexUtil;
import org.academy.internal.common.world.level.block.entity.WindGenPillarBlockEntity;

import static org.academy.api.client.resources.R.textures.block.wind_gen_pillar;

public final class WindGenPillarRenderer implements BlockEntityRenderer<WindGenPillarBlockEntity> {
    public static final WindGenPillarRenderer INSTANCE = new WindGenPillarRenderer();
    public static final RenderType PILLAR_RENDER_TYPE = RenderType.entitySolid(wind_gen_pillar);
    public static final float[][] PILLAR_VERTEX_BUFFER = VertexUtil.Cylinder.getCylinderVertexBuffer(0, 1, 0.3f, 8, true);
    public static final float[][] PILLAR_OUTLINE_VERTEX_BUFFER = VertexUtil.Cylinder.getCylinderWireframeBuffer(0, 1, 0.3f, 8);

    private WindGenPillarRenderer() {
    }

    public static void renderPillar(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0.5f, 0, 0.5f);
        poseStack.mulPose(Axis.YN.rotationDegrees(22.5f));
        var pose = poseStack.last();
        var vertexConsumer = bufferSource.getBuffer(PILLAR_RENDER_TYPE);
        CylinderRenderer.renderCylinder(pose, vertexConsumer, PILLAR_VERTEX_BUFFER, 1, 1, 1, 1, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    @Override
    public void render(WindGenPillarBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        renderPillar(poseStack, bufferSource, packedLight);
    }

    @Override
    public boolean shouldRenderOffScreen(WindGenPillarBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return Minecraft.getInstance().options.getEffectiveRenderDistance() * 16;
    }
}
