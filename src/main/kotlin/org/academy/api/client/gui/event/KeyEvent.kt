package org.academy.api.client.gui.event

import net.minecraft.Util

class KeyEvent(type: EventType, keyCode: Int, scanCode: Int, modifiers: Int) : InputEvent(type) {
    val keyCode: Int
    val scanCode: Int
    val modifiers: Int

    init {
        require(!(type != EventType.KEY_PRESSED && type != EventType.KEY_RELEASED)) { "This constructor is for KEY_PRESSED or KEY_RELEASED events." }
        this.keyCode = keyCode
        this.scanCode = scanCode
        this.modifiers = modifiers
    }

    fun hasAltDown(): Boolean {
        return (this.modifiers and 4) != 0
    }

    fun hasShiftDown(): Boolean {
        return (this.modifiers and 1) != 0
    }

    fun hasControlDown(): Boolean {
        return (this.modifiers and 2) != 0
    }

    fun hasControlDownWithQuirk(): Boolean {
        // 1.21.1 无 InputQuirks (26.2 新增): 内联其 EDIT_SHORTCUT_KEY_MODIFIER 逻辑.
        // macOS 用 Command(8), 其余平台用 Ctrl(2), 与 26.2 `REPLACE_CTRL_KEY_WITH_CMD_KEY = ON_OSX` 一致.
        val editShortcutModifier = if (Util.getPlatform() == Util.OS.OSX) 8 else 2
        return (this.modifiers and editShortcutModifier) != 0
    }
}
