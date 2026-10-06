package org.academy.internal.client.render.vfx;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import org.academy.api.client.renderer.EffectRenderer;

public final class WingFirstPersonBridge implements EffectRenderer {
    public static final WingFirstPersonBridge INSTANCE = new WingFirstPersonBridge();

    private WingFirstPersonBridge() {
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                       float yRot, float xRot) {
        // Third-person wings are rendered by WingVfx through the Vfx pipeline.
    }

    @Override
    public void renderFirstPerson(PoseStack poseStack, MultiBufferSource bufferSource,
                                  LocalPlayer player, int packedLight, float partialTick) {
        WingVfx.submitFirstPerson(poseStack, bufferSource, player, packedLight, partialTick);
    }

    @Override
    public boolean renderFirstPersonWhenHudHidden() {
        return WingVfx.renderFirstPersonWhenHudHidden();
    }
}
