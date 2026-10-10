package org.academy.internal.server.music.gametest;

import com.mojang.authlib.GameProfile;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import org.academy.AcademyCraft;
import org.academy.internal.common.music.SharedTrackEntry;
import org.academy.internal.server.config.MusicConfig;
import org.academy.internal.server.music.MusicRoomManager;
import org.academy.internal.server.music.MusicRoomStore;
import org.academy.internal.server.music.ServerJukeboxManager;

import java.util.UUID;

/**
 * 音乐系统服务端回归测试：验证音乐室/全服点播的播放启动与持久化行为喵。
 */
@EventBusSubscriber(modid = AcademyCraft.MOD_ID)
public final class MusicServerGameTests {
    @SubscribeEvent
    private static void registerTests(RegisterGameTestsEvent event) {
        event.register(MusicServerGameTests.class);
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void music_room_queue_add_starts_playback(GameTestHelper helper) {
        runScenario(helper, "room_queue_add_starts_playback");
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void music_room_idle_tick_advances(GameTestHelper helper) {
        runScenario(helper, "room_idle_tick_advances");
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void music_jukebox_request_starts_playback(GameTestHelper helper) {
        runScenario(helper, "jukebox_request_starts_playback");
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void music_room_persistence_round_trip(GameTestHelper helper) {
        runScenario(helper, "room_persistence_round_trip");
    }

    private static ServerPlayer createPlayer(GameTestHelper helper, String name) {
        var profile = new GameProfile(UUID.randomUUID(), name);
        var cookie = CommonListenerCookie.createInitial(profile, false);
        var player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(), profile,
                cookie.clientInformation());
        var connection = new Connection(PacketFlow.SERVERBOUND);
        new EmbeddedChannel(connection);
        NetworkRegistry.configureMockConnection(connection);
        helper.getLevel().getServer().getPlayerList().placeNewPlayer(connection, player, cookie);
        return player;
    }

    private static SharedTrackEntry track(String id, int durationSeconds) {
        return new SharedTrackEntry("netease", id, "Title " + id, "Artist", durationSeconds, false, "");
    }

    private static void withPlayer(GameTestHelper helper, String name, java.util.function.Consumer<ServerPlayer> body) {
        var player = createPlayer(helper, name);
        try {
            body.accept(player);
        } finally {
            helper.getLevel().getServer().getPlayerList().remove(player);
        }
    }

    private static void runScenario(GameTestHelper helper, String scenario) {
        var server = helper.getLevel().getServer();
        var config = new MusicConfig();
        MusicRoomManager.initServer(server, config);
        ServerJukeboxManager.initServer(server, config);
        switch (scenario) {
            case "room_queue_add_starts_playback" -> withPlayer(helper, "music-a", player -> {
                MusicRoomManager.create(player, "TestRoom");
                // 回归：修复前 queueAdd 只入队不开播，房间会一直静默喵.
                MusicRoomManager.queueAdd(player, track("1001", 120));
                var timeline = MusicRoomManager.timelineOf(player).orElse(null);
                helper.assertTrue(timeline != null,
                        "Adding a track to an idle room must start playback");
                helper.assertTrue("1001".equals(timeline.entry().trackId()),
                        "The queued track must become the current track");
                helper.assertTrue(MusicRoomManager.queueOf(player).entries().isEmpty(),
                        "The started track must leave the pending queue");
            });
            case "room_idle_tick_advances" -> withPlayer(helper, "music-b", player -> {
                MusicRoomManager.create(player, "TickRoom");
                MusicRoomManager.queueAdd(player, track("2001", 120));
                MusicRoomManager.next(player);
                var timeline = MusicRoomManager.timelineOf(player).orElse(null);
                helper.assertTrue(timeline == null,
                        "Skipping past the only track leaves the room idle");
                // 再入队一首后，空闲房间的 tick 兜底应自动续播喵.
                MusicRoomManager.queueAdd(player, track("2002", 120));
                var resumed = MusicRoomManager.timelineOf(player).orElse(null);
                helper.assertTrue(resumed != null
                                && "2002".equals(resumed.entry().trackId()),
                        "Queueing into an idle room restarts playback");
            });
            case "jukebox_request_starts_playback" -> withPlayer(helper, "music-c", player -> {
                ServerJukeboxManager.request(player, track("3001", 120));
                var timeline = ServerJukeboxManager.timeline().orElse(null);
                helper.assertTrue(timeline != null,
                        "A jukebox request must start playback for the server");
                helper.assertTrue("3001".equals(timeline.entry().trackId()),
                        "The requested track must be the current jukebox track");
            });
            case "room_persistence_round_trip" -> withPlayer(helper, "music-d", player -> {
                MusicRoomManager.create(player, "PersistRoom");
                MusicRoomManager.queueAdd(player, track("4001", 180));
                MusicRoomManager.saveState();
                var loaded = MusicRoomStore.load();
                helper.assertTrue(!loaded.isEmpty(),
                        "Saving must produce a readable room snapshot");
                var snapshot = loaded.get(0);
                helper.assertTrue("PersistRoom".equals(snapshot.name()),
                        "The room name must survive a save/load round trip");
                helper.assertTrue(snapshot.currentEntry() != null
                                && "4001".equals(snapshot.currentEntry().trackId()),
                        "The currently playing track must be persisted");
                helper.assertTrue(snapshot.members() != null && !snapshot.members().isEmpty(),
                        "Room members must be persisted");
            });
            default -> throw new IllegalArgumentException(scenario);
        }
        helper.succeed();
    }
}
