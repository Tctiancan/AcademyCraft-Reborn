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
import org.academy.internal.client.model.SolarGenModel;
import org.academy.internal.client.renderer.blockentity.state.SolarGenRenderState;

/**
 * 1.21.1 移植: 26.2 中 {@code solar_gen} 物品通过 {@code NoDataSpecialModelRenderer} 专属渲染
 * (见 {@code items/solar_gen.json} 的 {@code minecraft:special} / {@code academy:solar_gen}),
 * 该管线在 1.21.1 不存在, 因此改用 {@code BlockEntityWithoutLevelRenderer} 复用方块实体渲染模型。
 */
public final class SolarGenItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final SolarGenModel MODEL = new SolarGenModel(SolarGenModel.createBodyLayer().bakeRoot());
    private static SolarGenItemRenderer instance;

    private SolarGenItemRenderer(BlockEntityRenderDispatcher blockEntityRenderDispatcher, EntityModelSet entityModelSet) {
        super(blockEntityRenderDispatcher, entityModelSet);
    }

    public static SolarGenItemRenderer getInstance() {
        if (instance == null) {
            instance = new SolarGenItemRenderer(
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
        var renderState = new SolarGenRenderState();
        renderState.ageInTicks = 0;

        poseStack.translate(0.5F, 1.5F, 0.5F);
        poseStack.mulPose(Axis.XP.rotationDegrees(180));

        MODEL.setupAnim(renderState);
        MODEL.render(poseStack, bufferSource, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }
}
