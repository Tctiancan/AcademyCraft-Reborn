package org.academy.internal.client.model;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.AnimationState;
import org.joml.Vector3f;

/**
 * Applies a 1.21.1-style {@link AnimationDefinition} directly onto a {@link ModelPart} bone
 * tree by resolving the definition's bone names against the model's part hierarchy. This mirrors
 * the 26.2 behaviour of a baked {@code KeyframeAnimation#apply(AnimationState, float)} without
 * requiring a {@code HierarchicalModel}.
 */
public final class KeyframeAnimationApplier {
    private KeyframeAnimationApplier() {
    }

    /**
     * Advances an {@link AnimationState} by {@code ageInTicks} and applies {@code definition}
     * when the state has started, replicating 26.2 {@code KeyframeAnimation#apply(AnimationState, float)}.
     */
    public static void animateState(
            AnimationDefinition definition,
            AnimationState state,
            float ageInTicks,
            ModelPart root
    ) {
        state.updateTime(ageInTicks, 1.0F);
        if (state.isStarted()) {
            apply(definition, state.getAccumulatedTime(), 1.0F, root, new Vector3f());
        }
    }

    /**
     * Applies {@code definition} to the nearest bone descendants of {@code root}, using the same
     * interpolation logic as 1.21.1 {@code KeyframeAnimations#animate}.
     */
    public static void apply(
            AnimationDefinition definition,
            long accumulatedTime,
            float scale,
            ModelPart root,
            Vector3f vec
    ) {
        float elapsed = definition.looping()
                ? ((float) accumulatedTime / 1000.0F) % definition.lengthInSeconds()
                : (float) accumulatedTime / 1000.0F;

        for (var entry : definition.boneAnimations().entrySet()) {
            ModelPart part = findDescendant(root, entry.getKey());
            if (part == null) {
                continue;
            }
            for (AnimationChannel channel : entry.getValue()) {
                var target = channel.target();
                Keyframe[] keyframes = channel.keyframes();
                int i = Math.max(0,
                        Mth.binarySearch(0, keyframes.length,
                                index -> elapsed <= keyframes[index].timestamp()) - 1);
                Keyframe keyframe = keyframes[i];
                Keyframe keyframeNext;
                float delta;
                if (i + 1 >= keyframes.length) {
                    keyframeNext = keyframe;
                    delta = 0.0F;
                } else {
                    keyframeNext = keyframes[i + 1];
                    float span = keyframeNext.timestamp() - keyframe.timestamp();
                    delta = span <= 0.0F ? 0.0F
                            : Mth.clamp((elapsed - keyframe.timestamp()) / span, 0.0F, 1.0F);
                }
                keyframeNext.interpolation().apply(vec, delta, keyframes, i,
                        keyframeNext == keyframe ? i : i + 1, scale);
                target.apply(part, vec);
            }
        }
    }

    private static ModelPart findDescendant(ModelPart part, String name) {
        if (part.hasChild(name)) {
            return part.getChild(name);
        }
        for (ModelPart child : part.children.values()) {
            ModelPart found = findDescendant(child, name);
            if (found != null) {
                return found;
            }
        }
        return null;
    }
}
