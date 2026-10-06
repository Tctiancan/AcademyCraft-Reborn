package org.academy.internal.server.ability.gametest;

import com.mojang.authlib.GameProfile;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import org.academy.AcademyCraft;
import org.academy.api.common.ability.program.ProgramDirection;
import org.academy.api.server.ability.AbilitySystemServer;
import org.academy.internal.common.ability.AbilityCategories;
import org.academy.internal.common.ability.Skills;
import org.academy.internal.common.ability.aeromanip.charge.AeromanipChargeTier;
import org.academy.internal.common.ability.aeromanip.program.AeromanipProgramNodeCatalog;
import org.academy.internal.common.ability.aeromanip.program.ServerAeromanipProgramRuntime;
import org.academy.internal.common.ability.aeromanip.skills.lv3.LaminarCutter;
import org.academy.internal.common.ability.level0.skills.OutputControl;
import org.academy.internal.common.ability.program.ProgramPowerScale;
import org.academy.internal.common.skilldata.OutputControlData;

import java.util.List;
import java.util.UUID;

/** Verifies real, atomic CP/air payments for shared casts and Precision Operation nodes. */
@GameTestHolder(AcademyCraft.MOD_ID)
public final class AeromanipCostGameTests {
    private AeromanipCostGameTests() {}

    @GameTest(template = "empty", timeoutTicks = 200)
    static void aeromanipOutputCost(GameTestHelper helper) {
        verify(helper);
    }

    private static void verify(GameTestHelper helper) {
        var level = helper.getLevel();
        var profile = new GameProfile(UUID.randomUUID(), "air-cost-test");
        var cookie = CommonListenerCookie.createInitial(profile, false);
        var player = new ServerPlayer(level.getServer(), level, profile, cookie.clientInformation());
        var connection = new Connection(PacketFlow.SERVERBOUND);
        new EmbeddedChannel(connection);
        level.getServer().getPlayerList().placeNewPlayer(connection, player, cookie);
        player.setGameMode(GameType.SURVIVAL);
        try {
            var system = AbilitySystemServer.getSystem(player);
            system.setPlayerAbilityCategory(player.getUUID(), AbilityCategories.AEROMANIP.get());
            system.setPlayerLevel(player.getUUID(), 5);
            system.setPlayerBaseMaxCP(player.getUUID(), 100_000);
            system.setPlayerAvailableCP(player.getUUID(), 100_000);
            system.addPlayerSkill(player, Skills.LAMINAR_CUTTER.get().getKeyString());
            system.addPlayerSkill(player, Skills.OUTPUT_CONTROL.get().getKeyString());
            var cutterData = system.getPlayerData(player.getUUID()).getSkillDataMap()
                    .get(Skills.LAMINAR_CUTTER.get().getKeyString());
            cutterData.setEnabled(true);
            var output = (OutputControlData) system.getPlayerData(player.getUUID()).getSkillDataMap()
                    .get(Skills.OUTPUT_CONTROL.get().getKeyString());
            output.setEnabled(true);
            system.getAeromanipResourceManager().onPlayerLogin(player);
            var capacity = system.getAeromanipResourceManager().getCapacity(player);

            for (var tier : AeromanipChargeTier.values()) {
                var baseAir = switch (tier) {
                    case INSTANT -> 10.0f;
                    case HALF -> 18.0f;
                    case FULL -> 28.0f;
                };
                for (var power : List.of(0.01f, 0.5f, 1.0f, 2.0f)) {
                    resetResources(player, capacity);
                    output.setAbilityOutput(power);
                    helper.assertTrue(LaminarCutter.Server.tryProgramCast(
                                    player, new Vec3(0, 1, 0), -1, 1, -1, tier),
                            "Shared manual/default cast must succeed: " + tier + " power=" + power);
                    closeTo(helper, capacity - system.getAeromanipResourceManager().getCurrent(player),
                            baseAir * ProgramPowerScale.costMultiplier(power),
                            "Shared cast uses halved air cost and global output: " + tier);
                }

                // Real program execution bypasses global output and prices each node's own power.
                output.setAbilityOutput(2.0f);
                for (var power : List.of(0.01f, 0.5f, 1.0f, 2.0f)) {
                    resetResources(player, capacity);
                    var action = new ServerAeromanipProgramRuntime(player).laminarCut(
                            null, new ProgramDirection(0, 1, 0), power, tier, 1,
                            null, AeromanipProgramNodeCatalog.BladePlaneMode.DISABLED);
                    OutputControl.runWithoutOutputAdjustment(() -> {
                        try {
                            action.validate();
                            action.apply();
                        } catch (Exception error) {
                            throw new IllegalStateException("Precision node execution failed", error);
                        }
                    });
                    closeTo(helper, capacity - system.getAeromanipResourceManager().getCurrent(player),
                            baseAir * ProgramPowerScale.costMultiplier(power),
                            "Node uses its own output once: " + tier + " power=" + power);
                }
            }

            output.setAbilityOutput(0.01f);
            system.setPlayerCurrMP(player.getUUID(), capacity);
            helper.assertTrue(system.castContinuousCpAndMpIfPossible(player, Skills.ADIABATIC_COMPRESSION.get(),
                    unused808 -> 0, unused809 -> 10, (unused810, unused811) -> {}, true), "Continuous damage payment must succeed");
            closeTo(helper, capacity - system.getAeromanipResourceManager().getCurrent(player), 1,
                    "Continuous damage skills receive the same output air reduction");

            system.setPlayerCurrMP(player.getUUID(), capacity);
            helper.assertTrue(!Skills.BREATHING_BUBBLE.get().isOutputAdjustableDamage(),
                    "Non-damage control must remain a utility skill");
            helper.assertTrue(system.castContinuousCpAndMpIfPossible(player, Skills.BREATHING_BUBBLE.get(),
                    unused812 -> 0, unused813 -> 10, (unused814, unused815) -> {}, true), "Utility payment must succeed");
            closeTo(helper, capacity - system.getAeromanipResourceManager().getCurrent(player), 10,
                    "Utility air costs must not receive damage-skill output reduction");

            output.setEnabled(false);
            system.setPlayerCurrMP(player.getUUID(), capacity);
            helper.assertTrue(LaminarCutter.Server.tryProgramCast(
                    player, new Vec3(0, 1, 0), -1, 1, -1, AeromanipChargeTier.INSTANT),
                    "Cast without output control must succeed");
            closeTo(helper, capacity - system.getAeromanipResourceManager().getCurrent(player), 10,
                    "Disabled output control retains the halved base air cost");

            output.setEnabled(true);
            output.setAbilityOutput(0.01f);
            system.setPlayerCurrMP(player.getUUID(), 0.5f);
            var beforeCp = system.getPlayerAvailableCP(player.getUUID());
            helper.assertTrue(!LaminarCutter.Server.tryProgramCast(
                    player, new Vec3(0, 1, 0), -1, 1, -1, AeromanipChargeTier.INSTANT),
                    "Insufficient discounted air must reject the cast");
            closeTo(helper, system.getAeromanipResourceManager().getCurrent(player), 0.5f,
                    "Failed cast must not consume partial air");
            closeTo(helper, system.getPlayerAvailableCP(player.getUUID()), beforeCp,
                    "Failed cast must not occupy CP");
        } finally {
            level.getServer().getPlayerList().remove(player);
        }
        helper.succeed();
    }

    private static void resetResources(ServerPlayer player, float air) {
        var system = AbilitySystemServer.getSystem(player);
        // Each matrix entry is an independent cast, not a same-tick stack-limit stress test.
        system.getPlayerData(player.getUUID()).getMutableCpOccupations().clear();
        system.setPlayerAvailableCP(player.getUUID(), 100_000);
        system.setPlayerCurrMP(player.getUUID(), air);
    }

    private static void closeTo(GameTestHelper helper, float actual, float expected, String message) {
        helper.assertTrue(Math.abs(actual - expected) < 0.02f,
                message + ": expected " + expected + ", got " + actual);
    }
}
