package org.academy.api.client.input;

import java.util.List;

/**
 * 1.21.1 无 26.2 的 `net.minecraft.client.input.PreeditEvent` (IME preedit 抽象).
 *
 * 此处提供一个形状一致的垫层 record, 使既有形如 `event.fullText()` 的调用保持成立,
 * 属 C-stun: 1.21.1 不驱动 preedit 回调, 该对象仅为移植兼容而保留.
 */
public record PreeditEvent(String fullText, int caretPosition, List<String> blocks, int focusedBlock) {
}
