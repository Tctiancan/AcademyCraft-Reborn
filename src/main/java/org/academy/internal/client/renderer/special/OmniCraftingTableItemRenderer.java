package org.academy.internal.client.renderer.special;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.academy.internal.client.renderer.blockentity.OmniCraftingTableRenderer;
import org.academy.internal.client.renderer.blockentity.state.OmniCraftingTableRenderState;

/**
 * 1.21.1 移植: 26.2 中 {@code omni_crafting_table} 物品通过 {@code NoDataSpecialModelRenderer} 专属渲染
 * (OmniCraftingTableSpecialRenderer, scale 0.5 + 平移 (0.5,0,0.5) + XP180), 该管线在 1.21.1 不存在,
 * 因此改用 {@code BlockEntityWithoutLevelRenderer} 复用巨型工作台的多方块模型。
 */
public final class OmniCraftingTableItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static OmniCraftingTableItemRenderer instance;

    private OmniCraftingTableItemRenderer(BlockEntityRenderDispatcher blockEntityRenderDispatcher, EntityModelSet entityModelSet) {
        super(blockEntityRenderDispatcher, entityModelSet);
    }

    public static OmniCraftingTableItemRenderer getInstance() {
        if (instance == null) {
            instance = new OmniCraftingTableItemRenderer(
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
        var renderState = new OmniCraftingTableRenderState();
        renderState.ageInTicks = 0;
        renderState.isMain = true;
        renderState.facing = Direction.SOUTH;

        // 与 26.2 OmniCraftingTableSpecialRenderer 一致的姿态 (缩小 0.5 以适应物品体积)。
        poseStack.scale(0.5F, 0.5F, 0.5F);
        poseStack.translate(0.5F, 0F, 0.5F);
        poseStack.mulPose(Axis.XP.rotationDegrees(180));

        OmniCraftingTableRenderer.MODEL.setupAnim(renderState);
        OmniCraftingTableRenderer.MODEL.render(poseStack, bufferSource, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }
}
