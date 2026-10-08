package org.academy.internal.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.academy.internal.client.model.WirelessNodeModel;
import org.academy.internal.client.renderer.blockentity.state.WirelessNodeRenderState;
import org.academy.internal.common.world.level.block.entity.WirelessNodeBlockEntity;

public final class WirelessNodeRenderer implements BlockEntityRenderer<WirelessNodeBlockEntity> {
    public static final WirelessNodeRenderer INSTANCE = new WirelessNodeRenderer();
    public static final WirelessNodeModel MODEL = new WirelessNodeModel(WirelessNodeModel.createBodyLayer().bakeRoot());

    private WirelessNodeRenderer() {
    }

    @Override
    public void render(WirelessNodeBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        var renderState = new WirelessNodeRenderState();
        renderState.ageInTicks = blockEntity.ticks + partialTick;
        renderState.coreState = blockEntity.coreState;
        renderState.connectedUsersCount = blockEntity.connectedUsersCount;
        renderState.maxConnectedUsers = blockEntity.maxConnectedUsers;

        poseStack.pushPose();
        poseStack.translate(0.5f, 0, 0.5f);
        poseStack.rotateAround(Axis.XP.rotationDegrees(180), 0, 0, 0);
        MODEL.setupAnim(renderState);
        MODEL.render(poseStack, bufferSource, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(WirelessNodeBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return Minecraft.getInstance().options.getEffectiveRenderDistance() * 16;
    }
}
