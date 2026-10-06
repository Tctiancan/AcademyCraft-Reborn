package org.academy.api.client.render;

/**
 * std140 布局对齐助手, 迁自 Kotlin 顶层 roundToward 函数.
 */
final class Std140Util {
    private Std140Util() {
    }

    static int roundToward(int value, int alignment) {
        return alignment <= 0 ? value : (value + alignment - 1) / alignment * alignment;
    }
}
