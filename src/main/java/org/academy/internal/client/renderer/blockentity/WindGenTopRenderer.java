package org.academy.internal.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import org.academy.internal.client.model.WindGenTopModel;
import org.academy.internal.client.model.WindGenTurbineModel;
import org.academy.internal.client.renderer.blockentity.state.WindGenTopRenderState;
import org.academy.internal.common.world.level.block.entity.WindGenTopBlockEntity;

public final class WindGenTopRenderer implements BlockEntityRenderer<WindGenTopBlockEntity> {
    public static final WindGenTopRenderer INSTANCE = new WindGenTopRenderer();
    public static final WindGenTopModel MODEL = new WindGenTopModel(WindGenTopModel.createBodyLayer().bakeRoot());

    private WindGenTopRenderer() {
    }

    @Override
    public void render(WindGenTopBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        var renderState = new WindGenTopRenderState();
        renderState.ageInTicks = blockEntity.ticks + partialTick;
        renderState.facing = blockEntity.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        renderState.hasFan = blockEntity.hasFan;

        var facing = renderState.facing;
        var yRot = facing.getOpposite().toYRot();

        poseStack.pushPose();
        poseStack.translate(0.5f, 1.5f, 0.5f);
        poseStack.mulPose(Axis.XP.rotationDegrees(180));
        poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
        MODEL.render(poseStack, bufferSource, packedLight, OverlayTexture.NO_OVERLAY);

        if (renderState.hasFan) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.XP.rotationDegrees(180));
            poseStack.translate(0, -2.5f, -0.8f);
            poseStack.rotateAround(Axis.ZP.rotationDegrees(renderState.ageInTicks * 5), 0, 1.6125f, 0);
            WindGenTurbineModel.INSTANCE.render(poseStack, bufferSource, packedLight, OverlayTexture.NO_OVERLAY);
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    @Override
    public AABB getRenderBoundingBox(WindGenTopBlockEntity blockEntity) {
        return blockEntity.getRenderBoundingBox();
    }

    @Override
    public boolean shouldRenderOffScreen(WindGenTopBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return Minecraft.getInstance().options.getEffectiveRenderDistance() * 16;
    }
}
