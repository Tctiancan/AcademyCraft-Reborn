package org.academy.internal.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import org.academy.internal.client.renderer.blockentity.state.AbilityDeveloperRenderState;
import org.academy.internal.common.world.level.block.entity.AbilityDeveloperBlockEntity;

import static org.academy.internal.client.model.AbilityDeveloperModel.MODEL;

public final class AbilityDeveloperRenderer implements BlockEntityRenderer<AbilityDeveloperBlockEntity> {
    public static final AbilityDeveloperRenderer INSTANCE = new AbilityDeveloperRenderer();

    private AbilityDeveloperRenderer() {
    }

    @Override
    public void render(AbilityDeveloperBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (!blockEntity.isMain()) return;
        var renderState = new AbilityDeveloperRenderState();
        renderState.openingState = blockEntity.openingState;
        renderState.closingState = blockEntity.closingState;
        renderState.standingState = blockEntity.standingState;
        renderState.lyingDownState = blockEntity.lyingDownState;
        renderState.ageInTicks = blockEntity.ticks + partialTick;
        renderState.facing = blockEntity.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        renderState.isMain = blockEntity.isMain();

        var facing = renderState.facing;
        var yRot = facing.getOpposite().toYRot();

        poseStack.pushPose();
        poseStack.translate(0.5f, 1.5f, 0.5f);
        poseStack.mulPose(Axis.XP.rotationDegrees(180));
        poseStack.mulPose(Axis.YP.rotationDegrees(yRot + 180));
        poseStack.translate(0, 0, 1);
        MODEL.setupAnim(renderState);
        MODEL.render(poseStack, bufferSource, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    @Override
    public AABB getRenderBoundingBox(AbilityDeveloperBlockEntity blockEntity) {
        return blockEntity.getRenderBoundingBox();
    }

    @Override
    public boolean shouldRenderOffScreen(AbilityDeveloperBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return Minecraft.getInstance().options.getEffectiveRenderDistance() * 16;
    }
}
