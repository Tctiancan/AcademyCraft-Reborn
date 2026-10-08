package org.academy.internal.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import org.academy.internal.client.model.OmniCraftingTableModel;
import org.academy.internal.client.renderer.blockentity.state.OmniCraftingTableRenderState;
import org.academy.internal.common.world.level.block.entity.OmniCraftingTableBlockEntity;

public final class OmniCraftingTableRenderer implements BlockEntityRenderer<OmniCraftingTableBlockEntity> {
    public static final OmniCraftingTableRenderer INSTANCE = new OmniCraftingTableRenderer();
    public static final OmniCraftingTableModel MODEL = new OmniCraftingTableModel(OmniCraftingTableModel.createBodyLayer().bakeRoot());

    private OmniCraftingTableRenderer() {
    }

    @Override
    public void render(OmniCraftingTableBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (!blockEntity.isMain()) return;
        var renderState = new OmniCraftingTableRenderState();
        renderState.unfoldingState = blockEntity.unfoldingState;
        renderState.ageInTicks = blockEntity.ticks + partialTick;
        renderState.facing = blockEntity.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        renderState.isMain = blockEntity.isMain();

        var facing = renderState.facing;
        var yRot = facing.getOpposite().toYRot();

        poseStack.pushPose();
        poseStack.translate(0.5, 0, 0.5);
        poseStack.rotateAround(Axis.XP.rotationDegrees(180), 0, 0, 0);
        poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
        MODEL.setupAnim(renderState);
        MODEL.render(poseStack, bufferSource, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    @Override
    public AABB getRenderBoundingBox(OmniCraftingTableBlockEntity blockEntity) {
        return blockEntity.getRenderBoundingBox();
    }

    @Override
    public boolean shouldRenderOffScreen(OmniCraftingTableBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return Minecraft.getInstance().options.getEffectiveRenderDistance() * 16;
    }
}
