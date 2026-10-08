package org.academy.internal.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.academy.internal.client.model.SolarGenModel;
import org.academy.internal.client.renderer.blockentity.state.SolarGenRenderState;
import org.academy.internal.common.world.level.block.entity.SolarGenBlockEntity;

public final class SolarGenRenderer implements BlockEntityRenderer<SolarGenBlockEntity> {
    public static final SolarGenRenderer INSTANCE = new SolarGenRenderer();
    private static final SolarGenModel MODEL = new SolarGenModel(SolarGenModel.createBodyLayer().bakeRoot());

    private SolarGenRenderer() {
    }

    @Override
    public void render(SolarGenBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        var renderState = new SolarGenRenderState();
        renderState.ageInTicks = blockEntity.ticks + partialTick;
        renderState.facing = blockEntity.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        renderState.foldingState = blockEntity.foldingState;
        renderState.unfoldingState = blockEntity.unfoldingState;

        var facing = renderState.facing;
        var yRot = facing.getOpposite().toYRot();

        poseStack.pushPose();
        poseStack.translate(0.5f, 1.5f, 0.5f);
        poseStack.mulPose(Axis.XP.rotationDegrees(180));
        poseStack.mulPose(Axis.YP.rotationDegrees(yRot));

        MODEL.setupAnim(renderState);
        MODEL.render(poseStack, bufferSource, packedLight, OverlayTexture.NO_OVERLAY);

        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(SolarGenBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return Minecraft.getInstance().options.getEffectiveRenderDistance() * 16;
    }
}
