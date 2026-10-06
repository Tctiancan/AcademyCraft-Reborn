package org.academy.api.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;

@Deprecated
@FunctionalInterface
public interface EffectRenderer {
    void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, float yRot, float xRot);

    default void renderFirstPerson(PoseStack poseStack, MultiBufferSource bufferSource, LocalPlayer player, int packedLight, float partialTick) {

    }

    default boolean renderFirstPersonWhenHudHidden() {
        return false;
    }
}
