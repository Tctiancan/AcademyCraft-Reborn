package org.academy.api.client.input;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 1.21.1 无 26.2 的 `net.minecraft.client.input.KeyEvent` (键盘事件抽象).
 *
 * 此处提供一个形状一致的垫层 record, 使既有形如 `event.key()`/`event.scancode()`/`event.modifiers()` 的调用保持成立,
 * 属 C-stun: 1.21.1 的键盘回调走 `KeyboardHandler.keyPress` 的原始 int 参数, 该对象仅为移植兼容而保留.
 */
public record KeyEvent(
        int key,
        int scancode,
        @InputWithModifiers.Modifiers int modifiers
) implements InputWithModifiers {
    @Override
    public int input() {
        return key;
    }

    @Retention(RetentionPolicy.CLASS)
    @Target(ElementType.TYPE_USE)
    public @interface Action {
    }
}
