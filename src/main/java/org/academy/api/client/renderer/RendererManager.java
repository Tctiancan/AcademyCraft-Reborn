package org.academy.api.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;

import java.util.LinkedHashSet;
import java.util.Set;

public final class RendererManager {
    private static final Set<EffectRenderer> EFFECT_RENDERERS = new LinkedHashSet<>();

    public static void init() {
    }

    public static void registerEffectRenderer(EffectRenderer renderer) {
        EFFECT_RENDERERS.add(renderer);
    }

    public static void renderEffect(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, float yRot, float xRot) {
        for (var renderer : EFFECT_RENDERERS) {
            renderer.render(poseStack, bufferSource, packedLight, yRot, xRot);
        }
    }

    public static void renderEffectFirstPerson(PoseStack poseStack, MultiBufferSource bufferSource, LocalPlayer player, int packedLight, float partialTick) {
        for (var renderer : EFFECT_RENDERERS) {
            renderer.renderFirstPerson(poseStack, bufferSource, player, packedLight, partialTick);
        }
    }

    public static void renderEffectFirstPersonWithHiddenHud(PoseStack poseStack, MultiBufferSource bufferSource,
                                                            LocalPlayer player, int packedLight, float partialTick) {
        for (var renderer : EFFECT_RENDERERS) {
            if (renderer.renderFirstPersonWhenHudHidden()) {
                renderer.renderFirstPerson(poseStack, bufferSource, player, packedLight, partialTick);
            }
        }
    }
}
