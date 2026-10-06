package org.academy.internal.gui.map;

import net.minecraft.client.gui.GuiGraphics;
import org.academy.internal.common.ability.teleport.chunk.ChunkLeapPackets;

/**
 * Creates and submits type-cached, head-focused entity textures for the chunk-map radar.
 *
 * <p><b>C-stun (1.21.1):</b> the 26.2 entity-thumbnail path relied on the neoforge
 * Picture-in-Picture renderer ({@code PictureInPictureRenderer}/{@code SubmitNodeCollector}/
 * {@code CameraRenderState}), which does not exist in 1.21.1. The real previews are disabled for now:
 * {@link #render} always returns {@code false}, so the caller draws its plain category square instead.
 * The pure geometry/policy helpers below are kept intact (they have unit tests).
 */
public final class EntityRadarThumbnailRenderer {
    static final int MIN_DIAMETER = 6;
    private static final int MAX_SCALE = 32;
    private static final int CONTENT_INSET = 2;
    private static final int MAX_NEW_THUMBNAILS_PER_FRAME = 64;
    private static final float VERTICAL_NUDGE_PIXELS = 1.0f;

    void beginFrame() {
        // C-stun: thumbnail admission state removed.
    }

    void endFrame() {
        // C-stun: thumbnail admission state removed.
    }

    /**
     * C-stun: 1.21.1 没有 26.2 的 PIP 提交管线, 实体微缩图渲染在此禁用。始终返回 false,
     * 调用方 (ChunkMapRenderer) 会改画类别色块代替实体头像。
     */
    boolean render(GuiGraphics graphics, ChunkLeapPackets.Marker marker,
                   int centerX, int centerY, int diameter, boolean priority) {
        return false;
    }

    /** Fits the head region inside the square icon; very broad entities remain centered and intentionally cropped. */
    static int fitScale(double width, double height, int diameter) {
        var extent = Math.max(0.5, Math.min(Math.max(0.0, width), Math.max(0.0, height) * 0.45));
        var contentDiameter = Math.max(1, diameter - CONTENT_INSET);
        return Math.clamp((int) Math.floor(contentDiameter / extent), 1, MAX_SCALE);
    }

    static boolean canRenderThumbnail(boolean alreadyAdmitted, boolean priority, int admittedThisFrame) {
        return alreadyAdmitted || priority || admittedThisFrame < MAX_NEW_THUMBNAILS_PER_FRAME;
    }

    static float focusHeight(double height, double eyeHeight) {
        if (!Double.isFinite(height) || height <= 0.0) return 0.0f;
        if (!Double.isFinite(eyeHeight) || eyeHeight <= 0.0) return (float) (height * 0.5);
        return (float) Math.clamp(eyeHeight, 0.0, height);
    }

    /** Moves the model one logical pixel upward after centering it on the eyes. */
    static float adjustedFocusHeight(double height, double eyeHeight, int scale) {
        return Math.max(0.0f, focusHeight(height, eyeHeight)
                - VERTICAL_NUDGE_PIXELS / Math.max(1, scale));
    }

    /** Fallback colour by disposition: hostile red, neutral yellow, friendly green. */
    static int markerColor(byte category) {
        return switch (category) {
            case ChunkLeapPackets.CAT_HOSTILE -> 0xFFE16F68;
            case ChunkLeapPackets.CAT_PASSIVE -> 0xFF79C58D;
            case ChunkLeapPackets.CAT_NEUTRAL -> 0xFFF0CE7A;
            case ChunkLeapPackets.CAT_PLAYER -> 0xFF9FD8F0;
            case ChunkLeapPackets.CAT_ITEM -> 0xFFBFBFBF;
            case ChunkLeapPackets.CAT_PROJECTILE -> 0xFFF0A45A;
            default -> 0xFFB8B1A3;
        };
    }
}
