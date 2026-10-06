package org.academy.api.client.util;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import org.academy.api.client.ability.AbilitySystemClient;
import org.academy.api.client.gui.environment.UiEnvironment;
import org.academy.api.client.input.InputWithModifiers;
import org.academy.api.client.input.KeyEvent;
import org.academy.api.common.ability.Skill;
import org.academy.internal.common.sounds.SoundEvents;

public final class ClientUtil {
    private ClientUtil() {
    }

    public static boolean hasScreen() {
        return Minecraft.getInstance().screen != null;
    }

    public static boolean hasNoScreen() {
        return Minecraft.getInstance().screen == null;
    }

    public static boolean lacksSkill(Skill skill) {
        return !AbilitySystemClient.LEARNED_SKILLS.contains(skill);
    }

    public static float animationFactor(float animationDuration) {
        return UiEnvironment.get().frameDeltaTicks() / animationDuration;
    }

    public static double animationFactor(double animationDuration) {
        return UiEnvironment.get().frameDeltaTicks() / animationDuration;
    }

    public static void playDownSound() {
        var minecraft = Minecraft.getInstance();
        minecraft.getSoundManager().play(
                SimpleSoundInstance.forUI(SoundEvents.SELECT, 1.0F)
        );
    }

    public static ReentrantBlockableEventLoop<?> getRenderEventLoop() {
        return Minecraft.getInstance();
    }

    public static ReentrantBlockableEventLoop<?> getMainEventLoop() {
        return Minecraft.getInstance();
    }

    public static boolean isControlKey(
            @InputConstants.Value int key, int scancode, @InputWithModifiers.Modifiers int modifiers
    ) {
        var options = Minecraft.getInstance().options;
        var event = new KeyEvent(key, scancode, modifiers);
        var isHotbarKey = false;
        for (var hotbarKey : options.keyHotbarSlots) {
            if (hotbarKey.matches(event.key(), event.scancode())) {
                isHotbarKey = true;
                break;
            }
        }
        var isMovementKey
                = options.keyUp.matches(event.key(), event.scancode())
                || options.keyDown.matches(event.key(), event.scancode())
                || options.keyLeft.matches(event.key(), event.scancode())
                || options.keyRight.matches(event.key(), event.scancode())
                || options.keyJump.matches(event.key(), event.scancode())
                || options.keyShift.matches(event.key(), event.scancode())
                || options.keySprint.matches(event.key(), event.scancode());
        return isHotbarKey || isMovementKey;
    }
}
