package org.academy.internal.server.ability.gametest;

import com.mojang.authlib.GameProfile;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import org.academy.AcademyCraft;
import org.academy.api.common.damage.SkillDamageSource;
import org.academy.api.server.ability.HostileTargets;
import org.academy.internal.common.ability.Skills;
import org.academy.internal.common.ability.darkmatter.targeting.DarkmatterTargeting;
import org.academy.internal.common.ability.electromaster.skills.lv3.magneticweapon.MagneticWeaponAttackContext;
import org.academy.internal.common.attachment.AttachmentTypes;
import org.academy.internal.common.world.damagesource.CtaFriendlyFireWhitelist;
import org.academy.internal.common.world.damagesource.ReflectedSkillDamageSource;

import java.util.List;
import java.util.UUID;

/** Exercises real attack events, direct skill damage declarations, target selectors and physical expiry. */
@GameTestHolder(AcademyCraft.MOD_ID)
public final class HostileTargetGameTests {
    @GameTest(template = "empty", timeoutTicks = 400)
    static void hostileTargetRoutes(GameTestHelper helper) {
        withActors(helper, HostileTargetGameTests::routes);
    }

    @GameTest(template = "empty", timeoutTicks = 400)
    static void hostileTargetSelectors(GameTestHelper helper) {
        withActors(helper, HostileTargetGameTests::selectors);
    }

    @GameTest(template = "empty", timeoutTicks = 400)
    static void hostileTargetAutomatic(GameTestHelper helper) {
        withActors(helper, HostileTargetGameTests::automatic);
    }

    @GameTest(template = "empty", timeoutTicks = 400)
    static void hostileTargetExpiry(GameTestHelper helper) {
        var attacker = player(helper, 2);
        var other = player(helper, 8);
        expiry(helper, attacker, other, () -> {
            var players = helper.getLevel().getServer().getPlayerList();
            if (players.getPlayer(attacker.getUUID()) != null) players.remove(attacker);
            if (players.getPlayer(other.getUUID()) != null) players.remove(other);
        });
    }

    @GameTest(template = "empty", timeoutTicks = 400)
    static void hostileTargetProtection(GameTestHelper helper) {
        withActors(helper, HostileTargetGameTests::protection);
    }

    @GameTest(template = "empty", timeoutTicks = 400)
    static void hostileTargetCtaFriendlyFire(GameTestHelper helper) {
        withActors(helper, HostileTargetGameTests::ctaFriendlyFire);
    }

    @GameTest(template = "empty", timeoutTicks = 400)
    static void hostileTargetCleanup(GameTestHelper helper) {
        withActors(helper, HostileTargetGameTests::cleanup);
    }

    @FunctionalInterface
    private interface ActorScenario {
        void run(GameTestHelper helper, ServerPlayer attacker, ServerPlayer other);
    }

    private static void withActors(GameTestHelper helper, ActorScenario scenario) {
        var attacker = player(helper, 2);
        var other = player(helper, 8);
        try {
            scenario.run(helper, attacker, other);
            helper.succeed();
        } finally {
            var players = helper.getLevel().getServer().getPlayerList();
            if (players.getPlayer(attacker.getUUID()) != null) players.remove(attacker);
            if (players.getPlayer(other.getUUID()) != null) players.remove(other);
        }
    }

    static ServerPlayer player(GameTestHelper helper, int x) {
        var profile = new GameProfile(UUID.randomUUID(), "hostile-" + UUID.randomUUID().toString().substring(0, 8));
        var cookie = CommonListenerCookie.createInitial(profile, false);
        var player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(),
                profile, cookie.clientInformation());
        var connection = new Connection(PacketFlow.SERVERBOUND);
        new EmbeddedChannel(connection);
        helper.getLevel().getServer().getPlayerList().placeNewPlayer(connection, player, cookie);
        player.setGameMode(GameType.SURVIVAL);
        player.setData(AttachmentTypes.PVP_ENABLED.get(), true);
        var position = helper.absoluteVec(new Vec3(x, 3, 5));
        player.moveTo(position.x, position.y, position.z, 0, 0);
        player.setNoGravity(true);
        return player;
    }

    static void check(GameTestHelper helper, boolean condition, String message) {
        helper.assertTrue(condition, message);
    }

    private static LivingEntity pig(GameTestHelper helper) {
        var target = helper.spawn(EntityType.PIG, 5, 3, 5);
        target.setNoAi(true);
        target.setNoGravity(true);
        return target;
    }

    private static void routes(GameTestHelper helper, ServerPlayer attacker, ServerPlayer other) {
        var melee = pig(helper);
        attacker.attack(melee);
        check(helper, HostileTargets.isMarked(attacker, melee), "Melee must mark a neutral entity");
        check(helper, !HostileTargets.isMarked(other, melee), "Marks belong only to the attacking player");
        var arrow = helper.spawn(EntityType.ARROW, 5, 4, 5);
        var ranged = pig(helper);
        ranged.hurt( attacker.damageSources().arrow(arrow, attacker), 0.5f);
        check(helper, HostileTargets.isMarked(attacker, ranged), "Owned projectiles must mark on hit");
        var skillTarget = pig(helper);
        skillTarget.hurt( SkillDamageSource.of(attacker, Skills.ARC_GENERATE.get()), 0.5f);
        check(helper, HostileTargets.isMarked(attacker, skillTarget), "Direct category damage must mark on hit");
        var sweepTarget = pig(helper);
        sweepTarget.hurt( SkillDamageSource.of(attacker, Skills.IRON_SAND_ARSENAL.get()), 0.5f);
        check(helper, HostileTargets.isMarked(attacker, sweepTarget), "Manual attacks of mixed-mode skills must mark");
        var zero = pig(helper);
        zero.hurt( SkillDamageSource.of(attacker, Skills.ARC_GENERATE.get()), 0);
        check(helper, !HostileTargets.isMarked(attacker, zero), "Zero-damage skill calls must not mark");
        other.hurt( SkillDamageSource.of(attacker, Skills.ARC_GENERATE.get()), 0.5f);
        check(helper, HostileTargets.isMarked(attacker, other), "Skill hits must mark another player");
        check(helper, !HostileTargets.isMarked(other, attacker), "A mark must not be shared in reverse");
    }

    private static boolean selector(String type, String method, LivingEntity candidate, ServerPlayer owner) {
        try {
            var targetClass = Class.forName("org.academy.internal.common.ability." + type);
            var precision = method.equals("isHostileTo");
            var common = type.startsWith("program.");
            var signature = common ? new Class<?>[]{LivingEntity.class, LivingEntity.class, Object.class}
                    : precision ? new Class<?>[]{LivingEntity.class, LivingEntity.class}
                    : new Class<?>[]{ServerPlayer.class, LivingEntity.class};
            var selector = targetClass.getDeclaredMethod(method, signature);
            selector.setAccessible(true);
            return (boolean) (common ? selector.invoke(null, candidate, owner, null)
                    : precision ? selector.invoke(null, candidate, owner)
                    : selector.invoke(null, owner, candidate));
        } catch (ReflectiveOperationException error) {
            throw new IllegalStateException(error);
        }
    }

    private static void selectors(GameTestHelper helper, ServerPlayer owner, ServerPlayer target) {
        var cannon = "meltdowner.skills.lv5.AutoCruiseBeamCannon$Events";
        var magnetic = "electromaster.skills.lv3.MagneticWeapon$Context";
        check(helper, !selector(cannon, "isDetectable", target, owner), "Unmarked players must not be cruise targets");
        check(helper, !selector(magnetic, "isEnemy", target, owner), "Unmarked players must not be magnetic targets");
        check(helper, !selector("program.CommonProgramExecutors", "isHostileTo", target, owner),
                "Players start outside the common hostile filter");
        for (var candidate : List.of(target, pig(helper))) {
            check(helper, HostileTargets.mark(owner, candidate), "Explicit hostile action must admit neutral targets");
            check(helper, selector(cannon, "isDetectable", candidate, owner), "Cruise cannon must read the mark");
            check(helper, selector(magnetic, "isEnemy", candidate, owner), "Magnetic weapon must read the mark");
            check(helper, DarkmatterTargeting.isEnemyTarget(owner, candidate), "Darkmatter must read the mark");
            check(helper, selector("program.CommonProgramExecutors", "isHostileTo", candidate, owner),
                    "Common programs must read the mark in reference-to-candidate direction");
            check(helper, selector("mentalout.precision.PrecisionOperationRuntime", "isHostileTo", candidate, owner),
                    "Mental precision must read the same mark");
        }
    }

    private static void automatic(GameTestHelper helper, ServerPlayer attacker, ServerPlayer other) {
        for (var skill : List.of(Skills.AUTO_CRUISE_BEAM_CANNON.get(), Skills.ELECTRICAL_CONTACT.get(),
                Skills.LIGHT_SHIELD.get(), Skills.DARKMATTER_CREATION.get())) {
            var target = pig(helper);
            target.hurt( SkillDamageSource.of(attacker, skill), 0.1f);
            check(helper, !HostileTargets.isMarked(attacker, target), "Automatic skill must not mark: " + skill.getKey());
        }
        var pet = helper.spawn(EntityType.WOLF, 5, 3, 5);
        pet.tame(attacker);
        pet.setNoAi(true);
        pet.setNoGravity(true);
        var petTarget = pig(helper);
        petTarget.hurt( pet.damageSources().mobAttack(pet), 0.1f);
        check(helper, !HostileTargets.isMarked(attacker, petTarget), "Pet attacks must not create their owner's marks");
        var suppressed = pig(helper);
        var source = SkillDamageSource.of(attacker, Skills.ARC_GENERATE.get())
                .withoutHostilityMark().withElectricalChargePoints(1);
        check(helper, !source.canMarkHostility(), "Charge copies must retain automatic-hit attribution");
        check(helper, !SkillDamageSource.from(source, source.getSkill()).canMarkHostility(),
                "Converted secondary hits must preserve the automatic flag");
        suppressed.hurt( source, 0.1f);
        check(helper, !HostileTargets.isMarked(attacker, suppressed), "Explicit automatic hits must not mark");
        var reflected = pig(helper);
        reflected.hurt( ReflectedSkillDamageSource.from(
                SkillDamageSource.of(other, Skills.ARC_GENERATE.get()), attacker, Skills.ARC_GENERATE.get(), other), 0.1f);
        check(helper, !HostileTargets.isMarked(attacker, reflected), "Reflected damage must not mark");
        var magnetic = pig(helper);
        try {
            var open = MagneticWeaponAttackContext.class.getDeclaredMethod(
                    "open", ServerPlayer.class, LivingEntity.class, float.class);
            open.setAccessible(true);
            try (var ignored = (MagneticWeaponAttackContext) open.invoke(null, attacker, magnetic, 1.0f)) {
                attacker.attack(magnetic);
            }
        } catch (ReflectiveOperationException error) {
            throw new IllegalStateException(error);
        }
        check(helper, !HostileTargets.isMarked(attacker, magnetic), "Synthetic magnetic melee must not mark");
    }

    private static void expiry(GameTestHelper helper, ServerPlayer attacker, ServerPlayer other, Runnable finish) {
        var automaticTarget = pig(helper);
        var refreshedTarget = pig(helper);
        HostileTargets.mark(attacker, automaticTarget);
        HostileTargets.mark(attacker, refreshedTarget);
        helper.runAfterDelay(100, () -> {
            automaticTarget.hurt(
                    SkillDamageSource.of(attacker, Skills.AUTO_CRUISE_BEAM_CANNON.get()), 0.1f);
            NeoForge.EVENT_BUS.post(new AttackEntityEvent(attacker, refreshedTarget));
        });
        helper.runAfterDelay(199, () -> check(helper, HostileTargets.isMarked(attacker, automaticTarget),
                "Mark must last for all 199 preceding ticks"));
        helper.runAfterDelay(200, () -> {
            check(helper, !HostileTargets.isMarked(attacker, automaticTarget), "Automatic damage must not extend 200-tick expiry");
            check(helper, HostileTargets.isMarked(attacker, refreshedTarget), "Manual attack must refresh an existing mark");
        });
        helper.runAfterDelay(299, () -> check(helper, HostileTargets.isMarked(attacker, refreshedTarget),
                "Refresh must grant a complete second lifetime"));
        helper.runAfterDelay(300, () -> {
            try {
                check(helper, !HostileTargets.isMarked(attacker, refreshedTarget), "Refresh replaces rather than adds duration");
                helper.succeed();
            } finally {
                finish.run();
            }
        });
    }

    private static void protection(GameTestHelper helper, ServerPlayer attacker, ServerPlayer target) {
        check(helper, !HostileTargets.mark(attacker, attacker), "Self must never be marked");
        target.setGameMode(GameType.CREATIVE);
        check(helper, !HostileTargets.mark(attacker, target), "Creative players must remain protected");
        target.setGameMode(GameType.SPECTATOR);
        check(helper, !HostileTargets.mark(attacker, target), "Spectators must remain protected");
        target.setGameMode(GameType.SURVIVAL);
        target.setData(AttachmentTypes.PVP_ENABLED.get(), false);
        check(helper, !HostileTargets.mark(attacker, target), "PVP opt-out must block marking");
        target.setData(AttachmentTypes.PVP_ENABLED.get(), true);
        HostileTargets.mark(attacker, target);
        target.setData(AttachmentTypes.PVP_ENABLED.get(), false);
        check(helper, !HostileTargets.isMarked(attacker, target), "PVP changes must immediately override marks");
        target.setData(AttachmentTypes.PVP_ENABLED.get(), true);
        var scoreboard = helper.getLevel().getScoreboard();
        var team = scoreboard.addPlayerTeam("mark-" + UUID.randomUUID().toString().substring(0, 8));
        try {
            scoreboard.addPlayerToTeam(attacker.getScoreboardName(), team);
            scoreboard.addPlayerToTeam(target.getScoreboardName(), team);
            check(helper, !HostileTargets.isMarked(attacker, target), "Joining a team must override an earlier mark");
            check(helper, !HostileTargets.mark(attacker, target), "Teammates must not acquire new marks");
        } finally {
            scoreboard.removePlayerTeam(team);
        }
    }

    private static void ctaFriendlyFire(GameTestHelper helper, ServerPlayer attacker, ServerPlayer other) {
        var pet = helper.spawn(EntityType.WOLF, 5, 3, 5);
        pet.tame(attacker);
        pet.setNoAi(true);
        pet.setNoGravity(true);
        check(helper, CtaFriendlyFireWhitelist.shouldProtect(attacker, pet),
                "A calm tamed entity must retain CTA friendly-fire protection");

        pet.setTarget(other);
        check(helper, !CtaFriendlyFireWhitelist.shouldProtect(attacker, pet),
                "Hostility toward any player must disable CTA friendly-fire protection");

        pet.setTarget(null);
        pet.setLastHurtMob(other);
        check(helper, !CtaFriendlyFireWhitelist.shouldProtect(attacker, pet),
                "A tamed entity that recently attacked a player must remain damageable");

        pet.setLastHurtMob(null);
        check(helper, CtaFriendlyFireWhitelist.shouldProtect(attacker, pet),
                "CTA friendly-fire protection must return after player hostility clears");
        pet.discard();
    }

    private static void cleanup(GameTestHelper helper, ServerPlayer attacker, ServerPlayer other) {
        var target = pig(helper);
        HostileTargets.mark(attacker, target);
        target.discard();
        check(helper, !HostileTargets.isMarked(attacker, target), "Removed targets must no longer be marked");
        var neutralActor = pig(helper);
        var secondTarget = pig(helper);
        check(helper, HostileTargets.mark(neutralActor, secondTarget), "Public API must support non-player actors");
        check(helper, !HostileTargets.isMarked(attacker, secondTarget), "Non-player marks must remain isolated");
        var beforeLogout = pig(helper);
        HostileTargets.mark(attacker, beforeLogout);
        helper.getLevel().getServer().getPlayerList().remove(attacker);
        check(helper, !HostileTargets.isMarked(attacker, beforeLogout), "Logout must clear actor marks");
    }
}
