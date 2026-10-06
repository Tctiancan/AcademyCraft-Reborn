package org.academy.internal.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.academy.api.server.ability.electromaster.SupportReference;
import org.academy.internal.client.render.vfx.MagneticLevitationVfxClient;
import org.jspecify.annotations.Nullable;
import org.misaka.MisakaNetworkClient;
import org.misaka.MisakaNetworkServer;
import org.misaka.api.common.network.ThreadType;
import org.misaka.api.common.network.annotation.PacketTarget;
import org.misaka.api.common.network.annotation.SubscribePacket;
import org.misaka.api.common.network.packet.Packet;
import org.misaka.api.common.network.packet.PacketType;

/** The support used by the actual motion solver, refreshed every two ticks while hovering. */
@PacketTarget(ThreadType.CLIENT)
public final class MagneticSupportPacket extends Packet<ClientPacketListener, MagneticSupportPacket> {
    public static final StreamCodec<ByteBuf, MagneticSupportPacket> CODEC = StreamCodec.of((b, p) -> {
        ResourceLocation.STREAM_CODEC.encode(b, p.dimension);
        ByteBufCodecs.VAR_INT.encode(b, p.entityId);
        b.writeBoolean(p.active);
        if (p.active) {
            b.writeDouble(p.point.x).writeDouble(p.point.y).writeDouble(p.point.z);
            b.writeByte(p.face.ordinal());
        }
    }, b -> {
        var dimension = ResourceLocation.STREAM_CODEC.decode(b);
        int entity = ByteBufCodecs.VAR_INT.decode(b);
        boolean active = b.readBoolean();
        var point = active ? new Vec3(b.readDouble(), b.readDouble(), b.readDouble()) : Vec3.ZERO;
        int face = active ? b.readUnsignedByte() : Direction.UP.ordinal();
        if (entity < 0 || face >= Direction.values().length || !Double.isFinite(point.lengthSqr())) {
            throw new IllegalArgumentException("Invalid magnetic support");
        }
        return new MagneticSupportPacket(dimension, entity, active, point, Direction.values()[face]);
    });

    public final ResourceLocation dimension;
    public final int entityId;
    public final boolean active;
    public final Vec3 point;
    public final Direction face;

    public MagneticSupportPacket(ResourceLocation dimension, int entityId, boolean active, Vec3 point, Direction face) {
        this.dimension = dimension;
        this.entityId = entityId;
        this.active = active;
        this.point = point;
        this.face = face;
    }

    public static void broadcast(LivingEntity subject, @Nullable SupportReference support) {
        if (!(subject.level() instanceof ServerLevel level)) return;
        var packet = new MagneticSupportPacket(level.dimension().location(), subject.getId(), support != null,
                support == null ? Vec3.ZERO : support.closestPoint(), support == null ? Direction.UP : support.face());
        for (var observer : level.players()) {
            if (observer.distanceToSqr(subject) <= 96 * 96) MisakaNetworkServer.send(observer, packet);
        }
    }

    public static void initClient() { MisakaNetworkClient.NETWORK_MANAGER.register(Client.class); }
    @Override public PacketType<ClientPacketListener, MagneticSupportPacket> getPacketType() {
        return PacketTypes.MAGNETIC_SUPPORT.get();
    }
    public static final class Client {
        @SubscribePacket public static void handle(MagneticSupportPacket packet) { MagneticLevitationVfxClient.accept(packet); }
    }
}
