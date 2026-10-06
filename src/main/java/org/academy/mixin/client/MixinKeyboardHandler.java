package org.academy.mixin.client;

import net.minecraft.client.KeyboardHandler;
import org.academy.api.client.hud.terminal.TerminalHud;
import org.academy.api.client.input.CharacterEvent;
import org.academy.api.client.input.InputSystem;
import org.academy.api.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * For InputSystem
 *
 * 1.21.1 的 KeyboardHandler 键盘回调使用原始 int 参数 (key/scancode/action/modifiers),
 * 与 26.2 的 `keyPress(long, Action, KeyEvent)` 对象风格不同.
 * 此处重建 mod 本地垫层对象后交回 InputSystem; preedit(IME) 回调为 26.2 独有, 1.21.1 无对应物.
 */
@Mixin(KeyboardHandler.class)
public abstract class MixinKeyboardHandler {
    @Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
    private void keyPress(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        InputSystem.handleKey(action, new KeyEvent(key, scancode, modifiers), ci);
    }

    @Inject(method = "charTyped", at = @At("HEAD"), cancellable = true)
    private void charTyped(long window, int codepoint, int modifiers, CallbackInfo ci) {
        if (TerminalHud.Companion.handleCharacterInput(new CharacterEvent(codepoint))) {
            ci.cancel();
        }
    }
}
