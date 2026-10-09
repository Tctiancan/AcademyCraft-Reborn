package org.academy.internal.client.renderer.special;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.academy.api.client.resources.R;
import org.academy.internal.client.model.AbilityControlTabletModel;

import static org.academy.internal.client.model.AbilityControlTabletModel.MODEL;
import static org.academy.internal.client.model.AbilityControlTabletModel.SCREEN_MODEL;

/**
 * 1.21.1 移植: 26.2 中 {@code ability_control_tablet} 物品通过 {@code SpecialModelRenderer} 专属渲染
 * (AbilityControlTabletSpecialRenderer, 平移 (0.5,1.5,0.5) + XP180, 主体 + 自发光屏幕两层),
 * 该管线在 1.21.1 不存在, 因此改用 {@code BlockEntityWithoutLevelRenderer}。物品栏默认展示展开姿态。
 */
public final class AbilityControlTabletItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final long OPEN_ANIMATION_MILLIS = 459L;
    private static AbilityControlTabletItemRenderer instance;

    private AbilityControlTabletItemRenderer(BlockEntityRenderDispatcher blockEntityRenderDispatcher, EntityModelSet entityModelSet) {
        super(blockEntityRenderDispatcher, entityModelSet);
    }

    public static AbilityControlTabletItemRenderer getInstance() {
        if (instance == null) {
            instance = new AbilityControlTabletItemRenderer(
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
        var renderState = new AbilityControlTabletRenderState(OPEN_ANIMATION_MILLIS);

        // 与 26.2 AbilityControlTabletSpecialRenderer 一致的姿态。
        poseStack.translate(0.5F, 1.5F, 0.5F);
        poseStack.rotateAround(Axis.XP.rotationDegrees(180), 0, 0, 0);

        MODEL.setupAnim(renderState);
        MODEL.render(poseStack, bufferSource, RenderType.entityCutout(R.textures.model.ability_control_tablet),
                packedLight, OverlayTexture.NO_OVERLAY);

        SCREEN_MODEL.setupAnim(renderState);
        SCREEN_MODEL.render(poseStack, bufferSource, RenderType.entityTranslucent(R.textures.model.ability_control_tablet),
                LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }
}
