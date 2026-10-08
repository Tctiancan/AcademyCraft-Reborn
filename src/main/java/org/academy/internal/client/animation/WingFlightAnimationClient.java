package org.academy.internal.client.animation;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import org.academy.api.client.input.InputSystem;
import org.academy.internal.common.ability.accelerator.flight.WingFlightPose;
import org.academy.internal.common.attachment.AttachmentTypes;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Resolves synchronized wing controls into per-avatar animation playback.
 */
public final class WingFlightAnimationClient {
    private static final Map<UUID, WingFlightAnimationTimeline> TIMELINES = new HashMap<>();
    private static final Map<Integer, WingFlightAnimationTimeline.Playback> PLAYBACKS =
            new HashMap<>();

    private WingFlightAnimationClient() {
    }

    /**
     * 1.21.1 适配：26.2 的 3 参 prepareRenderState(Player, PlayerRenderState, F) 依赖不存在的
     * PlayerRenderState.isVisuallySwimming/swimAmount（仅 26.2 渲染状态可写）。1.21.1 动画播放层
     * 在 PlayerRenderer.render 中即时取样，视觉游泳的抑制由翼飞行姿态本身承担，此处实体化。
     */
    public static void prepareRenderState(Player avatar, float partialTick) {
        sample(avatar, partialTick);
    }

    public static WingFlightAnimationTimeline.Playback sample(
            Player avatar,
            float partialTick
    ) {
        var requested = requestedPose(avatar);
        var id = avatar.getUUID();
        var timeline = TIMELINES.computeIfAbsent(id, unused181 -> new WingFlightAnimationTimeline());
        var playback = timeline.update(requested, avatar.tickCount + partialTick);
        if (requested == WingFlightPose.Pose.IDLE
                && playback.phase() == WingFlightAnimationTimeline.Phase.IDLE) {
            TIMELINES.remove(id);
            PLAYBACKS.remove(avatar.getId());
        } else {
            PLAYBACKS.put(avatar.getId(), playback);
        }
        return playback;
    }

    public static WingFlightAnimationTimeline.Playback playback(int entityId) {
        return PLAYBACKS.get(entityId);
    }

    private static WingFlightPose.Pose requestedPose(Player avatar) {
        var minecraft = Minecraft.getInstance();
        if (avatar == minecraft.player && WingFlightPose.hasActiveWing(avatar)) {
            if (minecraft.screen != null) return WingFlightPose.Pose.IDLE;
            if (down(GLFW_KEY_SPACE)) return WingFlightPose.Pose.FAST;
            var forward = down(GLFW_KEY_W);
            var backward = down(GLFW_KEY_S);
            var left = down(GLFW_KEY_A);
            var right = down(GLFW_KEY_D);
            return forward != backward || left != right
                    ? WingFlightPose.Pose.SLOW : WingFlightPose.coastingPose(avatar);
        }
        return avatar.getData(AttachmentTypes.WING_FLIGHT_POSE.get());
    }

    private static boolean down(int key) {
        return InputSystem.isDown(InputSystem.InputType.KEYBOARD, key);
    }
}
