package org.academy.internal.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.phys.AABB;
import org.academy.internal.common.world.level.block.entity.WindGenBaseBlockEntity;

public final class WindGenBaseRenderer implements BlockEntityRenderer<WindGenBaseBlockEntity> {
    public static final WindGenBaseRenderer INSTANCE = new WindGenBaseRenderer();

    private WindGenBaseRenderer() {
    }

    @Override
    public void render(WindGenBaseBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        // C-stun: 26.2 渲染状态管线暂不移植，占位不渲染。
    }

    @Override
    public AABB getRenderBoundingBox(WindGenBaseBlockEntity blockEntity) {
        return blockEntity.getRenderBoundingBox();
    }

    @Override
    public boolean shouldRenderOffScreen(WindGenBaseBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return Minecraft.getInstance().options.getEffectiveRenderDistance() * 16;
    }
}
