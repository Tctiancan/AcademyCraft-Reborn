package org.academy.internal.client.renderer.blockentity.state;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.AnimationState;

public final class SolarGenRenderState {
    public float ageInTicks;
    public Direction facing = Direction.NORTH;
    public AnimationState foldingState = new AnimationState();
    public AnimationState unfoldingState = new AnimationState();
}
