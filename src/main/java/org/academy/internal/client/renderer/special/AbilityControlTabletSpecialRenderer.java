package org.academy.internal.client.renderer.special;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.academy.internal.common.world.item.Items;

import java.util.ArrayDeque;
import java.util.EnumMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * AbilityControlTablet 手持动画的渲染状态管理。
 *
 * 1.21.1 移植: 26.2 中该动画由 {@code SpecialModelRenderer} 的 {@code extractArgument} 在每次物品渲染前
 * 从线程本地 {@code CURRENT_ANIMATION_TIMES} 取出手持动画时间; 该管线在 1.21.1 不存在, 物品改用
 * {@code BlockEntityWithoutLevelRenderer} 渲染。此处保留 26.2 的静态入口 (tickHeldItems / prepareItemRender /
 * prepareThirdPersonRender), 由 [AbilityControlTabletItemRenderer] 在 {@code renderByItem} 中调用
 * [extractRenderState] 消费同一线程本地动画时间, 从而复现手持时的展开动画。
 */
public final class AbilityControlTabletSpecialRenderer {
    private static final long OPEN_ANIMATION_MILLIS = 459L;
    private static final Map<LivingEntity, EnumMap<InteractionHand, HandAnimationState>> HAND_ANIMATIONS =
            new WeakHashMap<>();
    private static final ThreadLocal<ArrayDeque<Long>> CURRENT_ANIMATION_TIMES = new ThreadLocal<>();

    private AbilityControlTabletSpecialRenderer() {
    }

    public static void tickHeldItems() {
        var level = Minecraft.getInstance().level;
        if (level == null) {
            HAND_ANIMATIONS.clear();
            return;
        }

        var now = System.nanoTime();
        for (var player : level.players()) {
            updateHandState(player, InteractionHand.MAIN_HAND, player.getMainHandItem(), now);
            updateHandState(player, InteractionHand.OFF_HAND, player.getOffhandItem(), now);
        }
    }

    public static void prepareItemRender(
            LivingEntity entity,
            ItemStack stack,
            ItemDisplayContext displayContext
    ) {
        var hand = resolveHand(entity, displayContext);
        if (hand == null) {
            CURRENT_ANIMATION_TIMES.remove();
            return;
        }

        var now = System.nanoTime();
        var state = updateHandState(entity, hand, stack, now);
        if (state.tabletSelected()) {
            var animationTimes = new ArrayDeque<Long>(1);
            animationTimes.add(animationTime(state, now));
            CURRENT_ANIMATION_TIMES.set(animationTimes);
        } else {
            CURRENT_ANIMATION_TIMES.remove();
        }
    }

    public static void prepareThirdPersonRender(LivingEntity entity) {
        var now = System.nanoTime();
        var animationTimes = new ArrayDeque<Long>(2);
        addThirdPersonAnimationTime(entity, HumanoidArm.RIGHT, now, animationTimes);
        addThirdPersonAnimationTime(entity, HumanoidArm.LEFT, now, animationTimes);
        if (animationTimes.isEmpty()) {
            CURRENT_ANIMATION_TIMES.remove();
        } else {
            CURRENT_ANIMATION_TIMES.set(animationTimes);
        }
    }

    /**
     * 消费当前手持动画时间并生成渲染状态, 等价于 26.2 {@code extractArgument}。
     * 无手持上下文 (如物品栏/掉落展示) 时返回完全展开姿态。
     */
    public static AbilityControlTabletRenderState extractRenderState() {
        var animationTimes = CURRENT_ANIMATION_TIMES.get();
        var animationTime = animationTimes != null ? animationTimes.pollFirst() : null;
        if (animationTimes == null || animationTimes.isEmpty()) CURRENT_ANIMATION_TIMES.remove();
        return new AbilityControlTabletRenderState(
                animationTime != null ? animationTime : OPEN_ANIMATION_MILLIS
        );
    }

    private static HandAnimationState updateHandState(
            LivingEntity entity,
            InteractionHand hand,
            ItemStack stack,
            long now
    ) {
        var tabletSelected = stack.is(Items.ABILITY_CONTROL_TABLET.get());
        var handStates = HAND_ANIMATIONS.computeIfAbsent(
                entity,
                ignored -> new EnumMap<>(InteractionHand.class)
        );
        var previous = handStates.get(hand);
        var animationStart = tabletSelected && (previous == null || !previous.tabletSelected())
                ? now
                : previous != null ? previous.animationStartNanos() : now;
        var current = new HandAnimationState(tabletSelected, animationStart);
        handStates.put(hand, current);
        return current;
    }

    private static InteractionHand resolveHand(LivingEntity entity, ItemDisplayContext displayContext) {
        var renderedArm = switch (displayContext) {
            case FIRST_PERSON_RIGHT_HAND, THIRD_PERSON_RIGHT_HAND -> HumanoidArm.RIGHT;
            case FIRST_PERSON_LEFT_HAND, THIRD_PERSON_LEFT_HAND -> HumanoidArm.LEFT;
            default -> null;
        };
        if (renderedArm == null) return null;
        return handForArm(entity, renderedArm);
    }

    private static void addThirdPersonAnimationTime(
            LivingEntity entity,
            HumanoidArm arm,
            long now,
            ArrayDeque<Long> animationTimes
    ) {
        var stack = entity.getItemInHand(handForArm(entity, arm));
        if (!stack.is(Items.ABILITY_CONTROL_TABLET.get())) return;
        var state = updateHandState(entity, handForArm(entity, arm), stack, now);
        animationTimes.add(animationTime(state, now));
    }

    private static InteractionHand handForArm(LivingEntity entity, HumanoidArm arm) {
        return arm == entity.getMainArm() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
    }

    private static long animationTime(HandAnimationState state, long now) {
        return Math.clamp((now - state.animationStartNanos()) / 1_000_000L, 0L,
                OPEN_ANIMATION_MILLIS);
    }

    private record HandAnimationState(boolean tabletSelected, long animationStartNanos) {
    }
}
