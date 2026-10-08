package org.academy.desktop.grapheditor.preview;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * 26.2 → 1.21.1 冒烟测试兼容助手 (C-stun/M21r).
 *
 * 26.2 的 {@code ServerPlayer.teleportTo(Level, x, y, z, Set<String>, int yaw, int pitch, boolean)}
 * 在 1.21.1 被拆分为 {@code teleportTo(ServerLevel, double, double, double, float, float)}。
 * 这里聚合出常用形态，减少各 Smoke 文件内的重复改写。
 */
final class SmokePort {
    private SmokePort() {
    }

    static void tp(ServerPlayer p, double x, double y, double z, float yaw, float pitch) {
        p.teleportTo((ServerLevel) p.level(), x, y, z, yaw, pitch);
    }

    static void tpInto(ServerPlayer p, ServerLevel level, double x, double y, double z, float yaw, float pitch) {
        p.teleportTo(level, x, y, z, yaw, pitch);
    }

    static ServerLevel levelOf(net.minecraft.world.entity.Entity entity) {
        return (ServerLevel) entity.level();
    }
}
