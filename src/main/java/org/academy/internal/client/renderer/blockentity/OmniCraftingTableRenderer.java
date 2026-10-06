package org.academy.internal.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.phys.AABB;
import org.academy.internal.common.world.level.block.entity.OmniCraftingTableBlockEntity;

public final class OmniCraftingTableRenderer implements BlockEntityRenderer<OmniCraftingTableBlockEntity> {
    public static final OmniCraftingTableRenderer INSTANCE = new OmniCraftingTableRenderer();

    private OmniCraftingTableRenderer() {
    }

    @Override
    public void render(OmniCraftingTableBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        // C-stun: 26.2 渲染状态管线暂不移植，占位不渲染。
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
