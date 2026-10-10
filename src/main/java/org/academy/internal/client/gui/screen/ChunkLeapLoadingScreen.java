package org.academy.internal.client.gui.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.network.chat.Component;

import java.util.function.BooleanSupplier;

/**
 * A {@link ReceivingLevelScreen} that draws no terrain-loading UI, used for 区块跃迁 transitions.
 *
 * <p>A cross-dimension teleport necessarily goes through {@code ClientboundRespawnPacket} → a rebuilt
 * {@code ClientLevel}, and the vanilla screen is what the load tracker displays while it waits.
 * Suppressing the whole tracking would break the handshake (the server waits for
 * {@code ServerboundPlayerLoadedPacket} before accepting movement), so the correct move is to keep the
 * tracker and replace only its appearance: the base class still ticks, still closes on
 * {@code isLevelReady()}, and still sends the loaded notification.
 *
 * <p>The background is intentionally not drawn — the previous frame remains until the new level is
 * ready, which is the closest a respawn-based transition can get to seamless. A brief centred label
 * explains the pause rather than leaving a frozen frame.
 */
public final class ChunkLeapLoadingScreen extends ReceivingLevelScreen {
    private static final Component LABEL = Component.translatable("chunk_leap.transition");

    public ChunkLeapLoadingScreen(BooleanSupplier levelReceived, Reason reason) {
        super(levelReceived, reason);
    }

    /** No terrain backdrop, no vanilla "Downloading Terrain" text: the last frame stays put. */
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Deliberately not calling super.render: that would draw the vanilla loading UI and a
        // background, which is exactly what this screen replaces.
        var centreX = this.width / 2;
        var centreY = this.height / 2;
        graphics.drawCenteredString(this.font, LABEL, centreX, centreY - 4, 0xFFC77DFF);
        // A restrained 120px rule under the label, in line with the mod's line-led visual language.
        graphics.fill(centreX - 60, centreY + 8, centreX + 60, centreY + 9, 0x60C77DFF);
    }

    @Override
    protected void updateNarratedWidget(net.minecraft.client.gui.narration.NarrationElementOutput output) {
        output.add(net.minecraft.client.gui.narration.NarratedElementType.TITLE, LABEL);
    }
}
