package org.academy.api.server.vanilla;

import net.minecraft.world.entity.Entity;
import org.academy.api.common.ability.Input;
import org.academy.internal.common.attachment.AttachmentTypes;

/**
 * 提供 26.2 {@code ServerPlayer.getLastClientInput()/setLastClientInput(Input)} 的 1.21.1 等价物。
 * <p>
 * 26.2 的 {@code ServerPlayer} 原生持有 {@code lastClientInput} 字段，并在处理
 * {@code ServerboundPlayerInputPacket} 时自动写入；1.21.1 无此字段，故以附件存储，
 * 由 {@code ServerGamePacketListenerImpl.handlePlayerInput} 的注入按需写入。
 */
public final class PlayerInputTracker {
    private PlayerInputTracker() {
    }

    public static Input get(Entity player) {
        var input = player.getData(AttachmentTypes.LAST_CLIENT_INPUT);
        return input == null ? Input.EMPTY : input;
    }

    public static void set(Entity player, Input input) {
        if (player == null) return;
        player.setData(AttachmentTypes.LAST_CLIENT_INPUT, input == null ? Input.EMPTY : input);
    }
}
