package org.academy.api.common.ability;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/**
 * mod 本地兼容垫层，替代 1.21.1 中不存在的 26.2 值对象
 * {@code net.minecraft.world.entity.player.Input}(不可变输入 record)。
 * <p>
 * 26.2 将该 record 直接存于 {@code LocalPlayer.input.keyPresses} 与
 * {@code ServerPlayer.lastClientInput}，并随客户端输入包整体传送；
 * 1.21.1 的客户端输入为可变布尔标志字段({@code up/down/left/right/jumping/shiftKeyDown})
 * 与 impulse 浮点字段，服务端亦无原生存储与包投递。
 * 本类保持 26.2 的字段形状与名称，便于各子系统沿用原逻辑。
 */
public record Input(boolean forward, boolean backward, boolean left, boolean right, boolean jump, boolean shift, boolean sprint) {
    private static final byte FLAG_FORWARD = 1;
    private static final byte FLAG_BACKWARD = 2;
    private static final byte FLAG_LEFT = 4;
    private static final byte FLAG_RIGHT = 8;
    private static final byte FLAG_JUMP = 16;
    private static final byte FLAG_SHIFT = 32;
    private static final byte FLAG_SPRINT = 64;

    public static final StreamCodec<FriendlyByteBuf, Input> STREAM_CODEC = new StreamCodec<FriendlyByteBuf, Input>() {
        public void encode(FriendlyByteBuf buffer, Input value) {
            byte flags = 0;
            flags = (byte) (flags | (value.forward() ? 1 : 0));
            flags = (byte) (flags | (value.backward() ? 2 : 0));
            flags = (byte) (flags | (value.left() ? 4 : 0));
            flags = (byte) (flags | (value.right() ? 8 : 0));
            flags = (byte) (flags | (value.jump() ? 16 : 0));
            flags = (byte) (flags | (value.shift() ? 32 : 0));
            flags = (byte) (flags | (value.sprint() ? 64 : 0));
            buffer.writeByte(flags);
        }

        public Input decode(FriendlyByteBuf buffer) {
            byte flags = buffer.readByte();
            return new Input(
                    (flags & 1) != 0,
                    (flags & 2) != 0,
                    (flags & 4) != 0,
                    (flags & 8) != 0,
                    (flags & 16) != 0,
                    (flags & 32) != 0,
                    (flags & 64) != 0
            );
        }
    };

    public static final Input EMPTY = new Input(false, false, false, false, false, false, false);
}
