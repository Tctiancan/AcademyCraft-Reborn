package org.academy.api.client.input;

/**
 * 1.21.1 无 26.2 的 `net.minecraft.client.input.CharacterEvent` (字符输入抽象).
 *
 * 此处提供一个形状一致的垫层 record, 使既有形如 `event.codepoint()` 的调用保持成立,
 * 属 C-stun: 1.21.1 的字符输入走 `Screen.charTyped`, 该对象仅为移植兼容而保留.
 */
public record CharacterEvent(int codepoint) {
}
