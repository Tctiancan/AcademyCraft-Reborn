package org.academy.internal.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.academy.internal.client.renderer.entity.state.BlockStructureRenderState;
import org.academy.internal.common.world.entity.structure.BlockStructureEntity;

/** Renders the captured block models in the structure entity's local coordinate space. */
public final class BlockStructureRenderer
        extends EntityRenderer<BlockStructureEntity> {
    public BlockStructureRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.0f;
    }

    @Override
    public void render(BlockStructureEntity entity,
                       float entityYaw,
                       float partialTick,
                       PoseStack poseStack,
                       MultiBufferSource bufferSource,
                       int packedLight) {
        var state = new BlockStructureRenderState();
        state.yawDegrees = entity.getYRot();
        var snapshot = entity.snapshot();
        state.pivotX = snapshot.pivotX();
        state.pivotZ = snapshot.pivotZ();
        state.updateBlocks(snapshot);

        poseStack.pushPose();
        poseStack.translate(state.pivotX, 0.0, state.pivotZ);
        poseStack.mulPose(Axis.YN.rotationDegrees(state.yawDegrees));
        poseStack.translate(-state.pivotX, 0.0, -state.pivotZ);
        var dispatcher = Minecraft.getInstance().getBlockRenderer();
        for (var block : state.blocks) {
            var position = block.relativePosition();
            poseStack.pushPose();
            poseStack.translate(position.getX(), position.getY(), position.getZ());
            dispatcher.renderSingleBlock(
                    block.state(),
                    poseStack,
                    bufferSource,
                    packedLight,
                    OverlayTexture.NO_OVERLAY
            );
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(BlockStructureEntity entity) {
        return MissingTextureAtlasSprite.getLocation();
    }
}
