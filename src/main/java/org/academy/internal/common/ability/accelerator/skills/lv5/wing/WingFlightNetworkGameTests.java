package org.academy.internal.common.ability.accelerator.skills.lv5.wing;

import com.mojang.authlib.GameProfile;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import org.academy.AcademyCraft;
import org.academy.api.common.ability.WingControlIntent;

import java.util.UUID;

/** Live server motion regression: packet aggregation must preserve every former directional push. */
@EventBusSubscriber(modid = AcademyCraft.MOD_ID)
public final class WingFlightNetworkGameTests {
    @SubscribeEvent private static void registerTests(RegisterGameTestsEvent event) {
        event.register(WingFlightNetworkGameTests.class);
    }
    @GameTest(template = "empty", timeoutTicks = 100)
    public static void wing_network_motion(GameTestHelper helper) {
        runMotion(helper);
    }
    private static void runMotion(GameTestHelper helper) {
        var profile = new GameProfile(UUID.randomUUID(), "wing-network");
        var cookie = CommonListenerCookie.createInitial(profile, false);
        var player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(), profile, cookie.clientInformation());
        var connection = new Connection(PacketFlow.SERVERBOUND);
        new EmbeddedChannel(connection);
        NetworkRegistry.configureMockConnection(connection);
        var players = helper.getLevel().getServer().getPlayerList();
        players.placeNewPlayer(connection, player, cookie);
        // markClientLoaded() was removed in 26.2 -> 1.21.1 port; no direct equivalent exists
        try {
            var system = org.academy.api.server.ability.AbilitySystemServer.getSystem(player);
            system.setPlayerAbilityCategory(player.getUUID(), org.academy.internal.common.ability.AbilityCategories.ACCELERATOR.get());
            system.setPlayerLevel(player.getUUID(), 5);
            var skill = org.academy.internal.common.ability.Skills.BLACK_WING.get();
            system.addPlayerSkill(player, skill.getKeyString());
            var data = system.getPlayerData(player.getUUID());
            data.setAcademyMaxCp(1_000_000);
            data.getCpData().setMaxCP(1_000_000);
            data.getCpData().setAvailableCP(1_000_000);
            if (!skill.isEnabled(player)) skill.toggle(player);
            for (int buttons : new int[]{0, 1, 2, 4, 8, 5, 9, 6, 10, 16}) {
                var input = new WingControlIntent(buttons, 123, 0);
                var initial = new Vec3(.3, .1, -.7);
                player.setDeltaMovement(initial);
                org.academy.internal.common.ability.accelerator.flight.WingFlightRuntime.clear(player, skill);
                for (int packet = 0; packet < 100; packet++) {
                    org.academy.internal.common.ability.accelerator.flight.WingFlightRuntime.accept(player, skill, input);
                }
                helper.assertTrue(player.getDeltaMovement().equals(initial), "Packets must not apply movement");
                player.tickCount++;
                org.academy.internal.common.ability.accelerator.flight.WingFlightRuntime.tick(player, skill);
                var once = player.getDeltaMovement();
                org.academy.internal.common.ability.accelerator.flight.WingFlightRuntime.tick(player, skill);
                helper.assertTrue(once.equals(player.getDeltaMovement()), "Duplicate tick must not apply another thrust");
                var expected = org.academy.api.common.ability.WingFlightMotion.step(
                        initial.multiply(.91, .98, .91), input, 0, 1, 1);
                helper.assertTrue(expected.distanceToSqr(player.getDeltaMovement()) < 1e-16,
                        "A stalled server must consume the newest input only once");
                org.academy.internal.common.ability.accelerator.flight.WingFlightRuntime.accept(
                        player, skill, new WingControlIntent(0, 0, 0, 0));
                player.tickCount++;
                org.academy.internal.common.ability.accelerator.flight.WingFlightRuntime.tick(player, skill);
                helper.assertTrue(player.getDeltaMovement().equals(Vec3.ZERO), "Zero momentum must stop immediately");
            }
            helper.succeed();
        } finally { players.remove(player); }
    }
}
