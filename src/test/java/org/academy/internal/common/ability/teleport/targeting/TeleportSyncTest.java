package org.academy.internal.common.ability.teleport.targeting;

import net.minecraft.world.entity.RelativeMovement;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TeleportSyncTest {
    @Test
    void vanillaTeleportPreservesClientViewWithZeroRelativeRotation() {
        assertEquals(
                Set.of(RelativeMovement.Y_ROT, RelativeMovement.X_ROT),
                TeleportSync.PRESERVED_VIEW_ROTATION
        );
    }
}
