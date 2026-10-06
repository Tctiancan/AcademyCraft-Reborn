package org.academy.mixin.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import org.academy.api.client.hud.terminal.TerminalHud;
import org.academy.internal.client.ability.mentalout.ControlledPlayerHudRenderer;
import org.academy.internal.client.ability.mentalout.MentalIntrusionClientState;
import org.academy.internal.client.ability.mentalout.MentalResistanceClientState;
import org.academy.internal.client.ability.mentalout.PlayerControlClientState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class MixinHud {
    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void extractCrosshair(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (TerminalHud.Companion.isActive()
                || MentalIntrusionClientState.isActive() && !PlayerControlClientState.isController()
                || PlayerControlClientState.isSubject()) ci.cancel();
    }

    @Inject(method = "renderHotbar", at = @At("HEAD"), cancellable = true)
    private void academy$replaceControlledHotbar(
            GuiGraphics graphics,
            DeltaTracker deltaTracker,
            CallbackInfo ci
    ) {
        if (PlayerControlClientState.isController()
                && PlayerControlClientState.targetViewState() != null) ci.cancel();
    }

    @Inject(method = {
            "maybeRenderExperienceBar",
            "renderExperienceLevel",
            "maybeRenderJumpMeter",
            "maybeRenderSelectedItemName"
    }, at = @At("HEAD"), cancellable = true)
    private void academy$replaceControlledStatus(
            GuiGraphics graphics,
            DeltaTracker deltaTracker,
            CallbackInfo ci
    ) {
        if (PlayerControlClientState.isController()
                && PlayerControlClientState.targetViewState() != null) ci.cancel();
    }

    @Inject(method = {
            "renderHealthLevel",
            "renderArmorLevel",
            "renderFoodLevel",
            "renderAirLevel"
    }, at = @At("HEAD"), cancellable = true)
    private void academy$replaceControlledPlayerHealth(
            GuiGraphics graphics,
            CallbackInfo ci
    ) {
        if (PlayerControlClientState.isController()
                && PlayerControlClientState.targetViewState() != null) ci.cancel();
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void academy$extractMentalPerceptionOverlay(
            GuiGraphics graphics,
            DeltaTracker deltaTracker,
            CallbackInfo ci
    ) {
        if (PlayerControlClientState.isController()) {
            ControlledPlayerHudRenderer.extract(graphics, deltaTracker);
        }
        if (!MentalIntrusionClientState.isActive() && !MentalIntrusionClientState.hasFilters()
                && !PlayerControlClientState.isActive()
                && !MentalResistanceClientState.isActive()) return;
        var width = graphics.guiWidth();
        var height = graphics.guiHeight();
        var pulse = (int) (36 + (Mth.sin((float) (System.nanoTime() * 0.000000003)) + 1.0) * 22);
        var color = (pulse << 24) | (MentalIntrusionClientState.hasFilters() ? 0xB45CE8 : 0x53C7E8);
        graphics.fill(0, 0, width, 3, color);
        graphics.fill(0, height - 3, width, height, color);
        graphics.fill(0, 3, 3, height - 3, color);
        graphics.fill(width - 3, 3, width, height - 3, color);
        if (PlayerControlClientState.isActive() || MentalResistanceClientState.isActive()) {
            var barWidth = Math.clamp(width / 5, 80, 160);
            var left = (width - barWidth) / 2;
            var top = PlayerControlClientState.isController() ? height - 65 : height - 18;
            graphics.fill(left - 1, top - 1, left + barWidth + 1, top + 7, 0xB0000000);
            var struggleProgress = MentalResistanceClientState.isActive()
                    ? MentalResistanceClientState.progress()
                    : PlayerControlClientState.struggle() / 100.0f;
            graphics.fill(left, top,
                    left + Math.round(barWidth * struggleProgress),
                    top + 6, 0xD8D95B6A);
            var maxCp = PlayerControlClientState.controllerMaxCp();
            if (maxCp > 0.0f) {
                graphics.fill(left, top + 9,
                        left + Math.round(barWidth * Mth.clamp(
                                PlayerControlClientState.controllerCp() / maxCp, 0.0f, 1.0f)),
                        top + 11, 0xD85CB9D9);
            }
        }
    }
}
