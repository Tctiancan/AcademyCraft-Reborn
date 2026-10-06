package org.academy.internal.server.ability.gametest;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.gametest.GameTestHolder;
import org.academy.AcademyCraft;
import org.academy.api.common.attribute.PlayerAttributes;
import org.academy.api.common.damage.SkillDamageSource;
import org.academy.api.server.ability.AbilitySystemServer;
import org.academy.internal.common.ability.AbilityCategories;
import org.academy.internal.common.ability.Skills;
import org.academy.internal.common.ability.mentalout.skills.lv4.PainSuppression;
import org.academy.internal.common.world.damagesource.DamageTypes;
import org.academy.internal.common.world.damagesource.SkillDamageUtil;

import static org.academy.internal.server.ability.gametest.HostileTargetGameTests.check;
import static org.academy.internal.server.ability.gametest.HostileTargetGameTests.player;

@GameTestHolder(AcademyCraft.MOD_ID)
public final class PainSuppressionGameTests {
    @GameTest(template = "empty", timeoutTicks = 200)
    static void painSuppressionToggle(GameTestHelper helper) {
        withPreparedTarget(helper, PainSuppressionGameTests::toggle);
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    static void painSuppressionDamage(GameTestHelper helper) {
        withPreparedTarget(helper, PainSuppressionGameTests::damage);
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    static void painSuppressionDirect(GameTestHelper helper) {
        withActors(helper, PainSuppressionGameTests::direct);
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    static void painSuppressionInsufficientCp(GameTestHelper helper) {
        withPreparedTarget(helper, PainSuppressionGameTests::insufficientCp);
    }

    @FunctionalInterface
    private interface TargetScenario {
        void run(GameTestHelper helper, ServerPlayer target);
    }

    private static void withPreparedTarget(GameTestHelper helper, TargetScenario scenario) {
        withActors(helper, (h, target, attacker) -> {
            prepare(target);
            scenario.run(h, target);
        });
    }

    private static void withActors(GameTestHelper helper, ActorsScenario scenario) {
        var target = player(helper, 2);
        var attacker = player(helper, 8);
        try {
            scenario.run(helper, target, attacker);
            helper.succeed();
        } finally {
            var players = helper.getLevel().getServer().getPlayerList();
            players.remove(target);
            players.remove(attacker);
        }
    }

    @FunctionalInterface
    private interface ActorsScenario {
        void run(GameTestHelper helper, ServerPlayer target, ServerPlayer attacker);
    }

    private static void prepare(ServerPlayer player) {
        var system = AbilitySystemServer.getSystem(player);
        var uuid = player.getUUID();
        system.setPlayerAbilityCategory(uuid, AbilityCategories.MENTALOUT.get());
        system.setPlayerLevel(uuid, 4);
        system.addPlayerSkill(player, Skills.PAIN_SUPPRESSION.get().getKeyString());
        var data = system.getPlayerData(uuid);
        data.setAcademyMaxCp(1000);
        data.getCpData().setMaxCP(1000);
        data.getCpData().setAvailableCP(1000);
        player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);
        player.setHealth(1000);
        PainSuppression.Server.setActive(player, true);
    }

    private static float intensity(ServerPlayer player) {
        return AbilitySystemServer.getSystem(player).getPlayerCalculationIntensity(player.getUUID());
    }

    private static float cp(ServerPlayer player) {
        return AbilitySystemServer.getSystem(player).getPlayerAvailableCP(player.getUUID());
    }

    private static void near(GameTestHelper helper, double expected, double actual, String message) {
        check(helper, Math.abs(expected - actual) < 0.001, message + ": expected=" + expected + ", actual=" + actual);
    }

    private static void toggle(GameTestHelper helper, ServerPlayer target) {
        near(helper, 1000 - 40 * intensity(target), cp(target), "Enabling reserves exactly 40 CP");
        near(helper, 8, target.getAttributeValue(PlayerAttributes.TRUE_RESISTANCE), "Enabling grants 8 resistance immediately");
        PainSuppression.Server.sync(target);
        near(helper, 1000 - 40 * intensity(target), cp(target), "Maintenance must not accumulate");
        target.getAttribute(PlayerAttributes.TRUE_RESISTANCE).setBaseValue(2);
        PainSuppression.Server.setActive(target, false);
        near(helper, 2, target.getAttributeValue(PlayerAttributes.TRUE_RESISTANCE), "Disabling removes only this skill's modifier");
        near(helper, 1000, cp(target), "Disabling releases maintenance CP");
        PainSuppression.Server.setActive(target, true);
        AbilitySystemServer.getSystem(target).setPlayerAbilityCategory(target.getUUID(), AbilityCategories.ELECTROMASTER.get());
        PainSuppression.Server.sync(target);
        near(helper, 2, target.getAttributeValue(PlayerAttributes.TRUE_RESISTANCE), "Changing category removes the skill modifier");
    }

    private static void hit(GameTestHelper helper, ServerPlayer target, Runnable action, float expectedDamage) {
        target.invulnerableTime = 0;
        var health = target.getHealth();
        var beforeCp = cp(target);
        action.run();
        var damage = health - target.getHealth();
        near(helper, expectedDamage, damage, "Final health loss must follow real resistance");
        near(helper, PainSuppression.damageCpCost(damage) * intensity(target), beforeCp - cp(target),
                "Each hit must charge once, using final damage");
    }

    private static void damage(GameTestHelper helper, ServerPlayer target) {
        var source = target.damageSources().generic();
        hit(helper, target, () -> target.hurt( source, 10), 2);
        hit(helper, target, () -> target.hurt( source, 15), 3);
        hit(helper, target, () -> target.hurt( source, 200), 40);
        target.getAttribute(Attributes.MAX_ABSORPTION).setBaseValue(20);
        target.setAbsorptionAmount(20);
        hit(helper, target, () -> target.hurt( source, 5), 0);
    }

    private static void direct(GameTestHelper helper, ServerPlayer target, ServerPlayer attacker) {
        var arc = SkillDamageSource.of(attacker, Skills.ARC_GENERATE.get());
        hit(helper, target, () -> target.hurt( arc, 15), 3);
        var melt = SkillDamageSource.of(attacker, Skills.SINGLE_HIGH_SPEED_ELECTRON_BEAM.get());
        hit(helper, target, () -> target.hurt( melt, 10), 10);
        var cta = SkillDamageSource.of(attacker, Skills.MIND_DESTRUCTION.get(), DamageTypes.CTA);
        hit(helper, target, () -> SkillDamageUtil.applyVerifiedTrueHealth(target, cta, 20), 7.2f);
    }

    private static void insufficientCp(GameTestHelper helper, ServerPlayer target) {
        var system = AbilitySystemServer.getSystem(target);
        system.getPlayerData(target.getUUID()).getCpData().setAvailableCP(1);
        target.hurt( target.damageSources().generic(), 15);
        check(helper, !Skills.PAIN_SUPPRESSION.get().isEnabled(target), "Unaffordable damage charge must disable the skill");
        near(helper, 0, target.getAttributeValue(PlayerAttributes.TRUE_RESISTANCE), "Failed payment must remove resistance immediately");
        near(helper, 1 + 40 * intensity(target), cp(target), "Failed payment must release maintenance without creating negative CP");
    }
}
