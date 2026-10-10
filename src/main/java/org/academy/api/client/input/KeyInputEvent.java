package org.academy.api.client.input;

public class KeyInputEvent extends InputControlEvent {
    public int key;
    public int scanCode;
    public @KeyEvent.Action int action;
    public @InputWithModifiers.Modifiers int modifiers;

    public KeyInputEvent(
            int key,
            int scanCode,
            @KeyEvent.Action int action,
            @InputWithModifiers.Modifiers int modifiers
    ) {
        this.key = key;
        this.scanCode = scanCode;
        this.action = action;
        this.modifiers = modifiers;
    }
}
