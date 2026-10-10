package org.academy.api.client.input;

import net.minecraft.Util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 1.21.1 无 26.2 的 `net.minecraft.client.input.InputWithModifiers` (带修饰键的输入抽象).
 *
 * 此处提供一个形状一致的垫层接口, 使既有调用保持成立,
 * 属 C-stun: 1.21.1 的各默认方法仅用于兼容保留.
 */
public interface InputWithModifiers {
    int NOT_DIGIT = -1;

    int MOD_SHIFT = 1;
    int MOD_CONTROL = 2;
    int MOD_ALT = 4;
    int MOD_SUPER = 8;
    int MOD_CAPS_LOCK = 1 << 16;
    int MOD_NUM_LOCK = 2 << 16;
    int EDIT_SHORTCUT_KEY_MODIFIER = Util.getPlatform() == Util.OS.OSX ? MOD_SUPER : MOD_CONTROL;

    @InputWithModifiers.Modifiers
    int modifiers();

    @InputWithModifiers.Modifiers
    int input();

    default boolean isSelection() {
        return input() == 257 || input() == 32 || input() == 335;
    }

    default boolean isConfirmation() {
        return input() == 257 || input() == 335;
    }

    default boolean isEscape() {
        return input() == 256;
    }

    default boolean isLeft() {
        return input() == 263;
    }

    default boolean isRight() {
        return input() == 262;
    }

    default boolean isUp() {
        return input() == 265;
    }

    default boolean isDown() {
        return input() == 264;
    }

    default boolean isCycleFocus() {
        return input() == 258;
    }

    default int getDigit() {
        int value = input() - 48;
        return value >= 0 && value <= 9 ? value : -1;
    }

    default boolean hasAltDown() {
        return (modifiers() & 4) != 0;
    }

    default boolean hasShiftDown() {
        return (modifiers() & 1) != 0;
    }

    default boolean hasControlDown() {
        return (modifiers() & 2) != 0;
    }

    default boolean hasControlDownWithQuirk() {
        return (modifiers() & EDIT_SHORTCUT_KEY_MODIFIER) != 0;
    }

    default boolean isSelectAll() {
        return input() == 65 && hasControlDownWithQuirk() && !hasShiftDown() && !hasAltDown();
    }

    default boolean isCopy() {
        return input() == 67 && hasControlDownWithQuirk() && !hasShiftDown() && !hasAltDown();
    }

    default boolean isPaste() {
        return input() == 86 && hasControlDownWithQuirk() && !hasShiftDown() && !hasAltDown();
    }

    default boolean isCut() {
        return input() == 88 && hasControlDownWithQuirk() && !hasShiftDown() && !hasAltDown();
    }

    @Retention(RetentionPolicy.CLASS)
    @Target(ElementType.TYPE_USE)
    @interface Modifiers {
    }
}
