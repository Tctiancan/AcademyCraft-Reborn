package org.academy.internal.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import org.academy.internal.client.model.WindGenBaseModel;
import org.academy.internal.client.renderer.blockentity.state.WindGenBaseRenderState;
import org.academy.internal.common.world.level.block.entity.WindGenBaseBlockEntity;

public final class WindGenBaseRenderer implements BlockEntityRenderer<WindGenBaseBlockEntity> {
    public static final WindGenBaseRenderer INSTANCE = new WindGenBaseRenderer();

    private WindGenBaseRenderer() {
    }

    @Override
    public void render(WindGenBaseBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        var renderState = new WindGenBaseRenderState();
        renderState.ageInTicks = blockEntity.ticks + partialTick;
        renderState.facing = blockEntity.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        renderState.isMain = blockEntity.isMain();
        renderState.setupState = blockEntity.setupState;
        renderState.shutdownState = blockEntity.shutdownState;

        if (blockEntity.isMain()) {
            poseStack.pushPose();
            var yRot = renderState.facing.getOpposite().toYRot();
            poseStack.pushPose();
            poseStack.scale(1, 15 / 16f, 1);
            poseStack.translate(0, 1 / 16f, 0);
            WindGenPillarRenderer.renderPillar(poseStack, bufferSource, packedLight);
            poseStack.popPose();
            poseStack.translate(0.5f, 0f, 0.5f);
            poseStack.translate(0, 1.5f, 0);
            poseStack.mulPose(Axis.XP.rotationDegrees(180));
            poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
            WindGenBaseModel.MODEL.setupAnim(renderState);
            WindGenBaseModel.MODEL.render(poseStack, bufferSource, packedLight, OverlayTexture.NO_OVERLAY);
            poseStack.popPose();
        } else {
            WindGenPillarRenderer.renderPillar(poseStack, bufferSource, packedLight);
        }
    }

    @Override
    public AABB getRenderBoundingBox(WindGenBaseBlockEntity blockEntity) {
        return blockEntity.getRenderBoundingBox();
    }

    @Override
    public boolean shouldRenderOffScreen(WindGenBaseBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return Minecraft.getInstance().options.getEffectiveRenderDistance() * 16;
    }
}
