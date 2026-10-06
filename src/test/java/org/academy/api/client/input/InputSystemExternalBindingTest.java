package org.academy.api.client.input;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InputSystemExternalBindingTest {
    private static final ResourceLocation INVERSE_REASONING =
            ResourceLocation.fromNamespaceAndPath("academy_inverse_reasoning", "inverse_reasoning");

    @Test
    void namespacedAddonBindingBelongsToItsSkill() {
        assertTrue(InputSystem.isBindingForSkill(
                "key.academy_inverse_reasoning.inverse_reasoning.analyze",
                INVERSE_REASONING
        ));
        assertFalse(InputSystem.isBindingForSkill(
                "key.other_mod.inverse_reasoning.analyze",
                INVERSE_REASONING
        ));
    }

    @Test
    void legacyCoreBindingNamesRemainSupported() {
        var vectorReflection = ResourceLocation.fromNamespaceAndPath("academy", "vector_reflection");
        assertTrue(InputSystem.isBindingForSkill("vector_reflection_toggle", vectorReflection));
        assertTrue(InputSystem.isBindingForSkill("vector_reflection.toggle", vectorReflection));
    }
}
