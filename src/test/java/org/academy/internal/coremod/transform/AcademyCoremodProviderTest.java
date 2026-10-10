package org.academy.internal.coremod.transform;

import cpw.mods.modlauncher.api.ITransformer;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AcademyCoremodProviderTest {
    @Test
    void serviceRegistersThreeTargetedTransformers() {
        var provider = new AcademyCoremodProvider();
        var transformers = provider.transformers();

        assertEquals("academy_coremod", provider.name());
        assertEquals(3, transformers.size());

        var expectedClasses = Set.of(
                "net.minecraft.server.level.ServerLevel",
                "net.minecraft.client.multiplayer.ClientLevel",
                "net.minecraft.client.KeyboardHandler",
                "net.minecraft.client.MouseHandler",
                "org.academy.api.client.input.InputSystem",
                "net.minecraft.world.level.levelgen.feature.EndSpikeFeature",
                "net.minecraft.world.level.levelgen.feature.EndSpikeFeature$EndSpike",
                "net.minecraft.world.level.levelgen.feature.EndPodiumFeature",
                "net.minecraft.world.entity.LivingEntity"
        );

        var actual = transformers.stream()
                .flatMap(transformer -> transformer.targets().stream())
                .collect(Collectors.toSet());
        var expected = expectedClasses.stream()
                .map(ITransformer.Target::targetClass)
                .collect(Collectors.toSet());

        assertEquals(expected, actual);
    }
}
