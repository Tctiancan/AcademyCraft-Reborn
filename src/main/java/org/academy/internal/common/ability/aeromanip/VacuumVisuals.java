package org.academy.internal.common.ability.aeromanip;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.academy.internal.common.network.PacketTypes;
import org.misaka.MisakaNetworkClient;
import org.misaka.MisakaNetworkServer;
import org.misaka.api.common.network.ThreadType;
import org.misaka.api.common.network.annotation.PacketTarget;
import org.misaka.api.common.network.annotation.SubscribePacket;
import org.misaka.api.common.network.packet.Packet;
import org.misaka.api.common.network.packet.PacketType;
import java.util.*;

/** One following visual per owner; heartbeat renews the lease without replaying particle bursts. */
public final class VacuumVisuals {
    public static void initClient() { MisakaNetworkClient.NETWORK_MANAGER.register(Client.class); }
    public static final class Lease implements AutoCloseable {
        private final ServerPlayer owner;
        private final ServerLevel level;
        private final Set<ServerPlayer> observers = Collections.newSetFromMap(new IdentityHashMap<>());
        private long nextSync;
        private float radius = -1;
        public Lease(ServerPlayer owner) { this.owner = owner; level = owner.serverLevel(); }
        public void update(float radius) {
            long now = level.getGameTime();
            if (now < nextSync && this.radius == radius) return;
            this.radius = radius;
            nextSync = now + 20;
            var update = packet(true);
            var iterator = observers.iterator();
            while (iterator.hasNext()) {
                var observer = iterator.next();
                if (observer.hasDisconnected() || observer.level() != level || observer.distanceToSqr(owner) > 112 * 112) {
                    if (!observer.hasDisconnected()) MisakaNetworkServer.send(observer, packet(false));
                    iterator.remove();
                } else MisakaNetworkServer.send(observer, update);
            }
            for (var observer : level.players()) {
                if (observer.distanceToSqr(owner) > 108 * 108) continue;
                if (observers.add(observer)) MisakaNetworkServer.send(observer, update);
            }
        }
        private Update packet(boolean active) {
            return new Update(level.dimension().location(), owner.getId(), owner.getUUID(), owner.position(),
                    Math.max(1, radius), active);
        }
        @Override public void close() {
            var stop = packet(false);
            for (var observer : observers) if (!observer.hasDisconnected()) MisakaNetworkServer.send(observer, stop);
            observers.clear();
        }
    }
    @PacketTarget(ThreadType.CLIENT)
    public static final class Update extends Packet<ClientPacketListener, Update> {
        public final ResourceLocation dimension;
        public final int ownerId;
        public final UUID owner;
        public final Vec3 position;
        public final float radius;
        public final boolean active;
        public static final StreamCodec<ByteBuf, Update> CODEC = StreamCodec.of((b, p) -> {
            ResourceLocation.STREAM_CODEC.encode(b, p.dimension); b.writeInt(p.ownerId);
            b.writeLong(p.owner.getMostSignificantBits()).writeLong(p.owner.getLeastSignificantBits());
            b.writeDouble(p.position.x).writeDouble(p.position.y).writeDouble(p.position.z); b.writeFloat(p.radius).writeBoolean(p.active);
        }, b -> new Update(ResourceLocation.STREAM_CODEC.decode(b), b.readInt(), new UUID(b.readLong(), b.readLong()),
                new Vec3(b.readDouble(), b.readDouble(), b.readDouble()), b.readFloat(), b.readBoolean()));
        public Update(ResourceLocation dimension, int ownerId, UUID owner, Vec3 position, float radius, boolean active) {
            if (!Float.isFinite(radius) || radius < 1 || radius > 12) throw new IllegalArgumentException("Visual radius");
            this.dimension = dimension; this.ownerId = ownerId; this.owner = owner;
            this.position = position; this.radius = radius; this.active = active;
        }
        @Override public PacketType<ClientPacketListener, Update> getPacketType() { return PacketTypes.VACUUM_VISUAL.get(); }
    }
    public static final class Client {
        @SubscribePacket public static void receive(Update packet) {
            org.academy.internal.client.ability.aeromanip.VacuumVisualClient.receive(packet);
        }
    }
}
