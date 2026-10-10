package org.academy.internal.client.renderer.special;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.academy.api.client.resources.R;

import static org.academy.internal.client.model.ImagPhaseDowsingRodModel.MODEL;

/**
 * 1.21.1 移植: 26.2 中 {@code imag_phase_dowsing_rod} 物品通过 {@code SpecialModelRenderer} 专属渲染
 * (ImagPhaseDowsingRodSpecialRenderer), 该管线在 1.21.1 不存在, 因此改用 {@code BlockEntityWithoutLevelRenderer}。
 * 手持时按 26.2 的握持姿态渲染; 物品栏/掉落/展示使用摆放姿态。26.2 中仅在玩家手持且面向世界时
 * 才会叠加实时侦查地图几何, 此处在物品渲染中不绘制。
 */
public final class ImagPhaseDowsingRodItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final float GRIP_X = 1.3F / 16.0F;
    private static final float GRIP_Y = 17.8F / 16.0F;
    private static final float THIRD_PERSON_SCALE = 0.475F;
    private static final float FIRST_PERSON_SCALE = 0.7125F;
    private static ImagPhaseDowsingRodItemRenderer instance;

    private ImagPhaseDowsingRodItemRenderer(BlockEntityRenderDispatcher blockEntityRenderDispatcher, EntityModelSet entityModelSet) {
        super(blockEntityRenderDispatcher, entityModelSet);
    }

    public static ImagPhaseDowsingRodItemRenderer getInstance() {
        if (instance == null) {
            instance = new ImagPhaseDowsingRodItemRenderer(
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
        applyModelTransform(displayContext, poseStack);
        MODEL.render(poseStack, bufferSource, RenderType.entityCutout(R.textures.IMAG_PHASE_DOWSING_ROD),
                packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    private static void applyModelTransform(ItemDisplayContext displayContext, PoseStack poseStack) {
        if (isHeld(displayContext)) {
            poseStack.translate(0.5F, 0.5F, 0.5F);
            poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
            if (!displayContext.firstPerson()) {
                poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
            }
            var modelScale = displayContext.firstPerson()
                    ? FIRST_PERSON_SCALE
                    : THIRD_PERSON_SCALE;
            poseStack.scale(modelScale, modelScale, modelScale);
            poseStack.translate(-GRIP_X, -GRIP_Y, 0.0F);
            return;
        }
        poseStack.translate(0.5F, 1.5F, 0.5F);
        poseStack.rotateAround(Axis.XP.rotationDegrees(180.0F), 0.0F, 0.0F, 0.0F);
    }

    private static boolean isHeld(ItemDisplayContext displayContext) {
        return displayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                || displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                || displayContext == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND
                || displayContext == ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
    }
}
