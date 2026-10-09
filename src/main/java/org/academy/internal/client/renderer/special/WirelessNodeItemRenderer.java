package org.academy.internal.client.renderer.special;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.academy.internal.client.renderer.blockentity.WirelessNodeRenderer;
import org.academy.internal.client.renderer.blockentity.state.WirelessNodeRenderState;

/**
 * 1.21.1 移植: 26.2 中 {@code wireless_node} 物品通过 {@code NoDataSpecialModelRenderer} 专属渲染
 * (WirelessNodeSpecialRenderer, 平移 (0.5,0,0.5) + XP180), 该管线在 1.21.1 不存在, 因此改用
 * {@code BlockEntityWithoutLevelRenderer} 复用无线节点的模型。未连接用户时按静态姿态渲染。
 */
public final class WirelessNodeItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static WirelessNodeItemRenderer instance;

    private WirelessNodeItemRenderer(BlockEntityRenderDispatcher blockEntityRenderDispatcher, EntityModelSet entityModelSet) {
        super(blockEntityRenderDispatcher, entityModelSet);
    }

    public static WirelessNodeItemRenderer getInstance() {
        if (instance == null) {
            instance = new WirelessNodeItemRenderer(
                    Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                    Minecraft.getInstance().getEntityModels()
            );
        }
        return instance;
    }

    @Override
    public void renderByItem(
            ItemStack stack,
            ItemDisplayContext displayContext,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay
    ) {
        poseStack.pushPose();
        var renderState = new WirelessNodeRenderState();
        renderState.ageInTicks = 0;
        renderState.maxConnectedUsers = 1;
        renderState.connectedUsersCount = 0;

        // 与 26.2 WirelessNodeSpecialRenderer / 方块实体渲染器一致的姿态。
        poseStack.translate(0.5F, 0F, 0.5F);
        poseStack.rotateAround(Axis.XP.rotationDegrees(180), 0, 0, 0);

        WirelessNodeRenderer.MODEL.setupAnim(renderState);
        WirelessNodeRenderer.MODEL.render(poseStack, bufferSource, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }
}
