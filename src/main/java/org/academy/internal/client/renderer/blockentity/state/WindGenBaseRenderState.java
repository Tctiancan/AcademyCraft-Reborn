package org.academy.internal.client.renderer.blockentity.state;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.AnimationState;

public final class WindGenBaseRenderState {
    public static final WindGenBaseRenderState NONE = new WindGenBaseRenderState();
    public boolean isMain;
    public Direction facing = Direction.NORTH;
    public float ageInTicks;
    public AnimationState setupState = new AnimationState();
    public AnimationState shutdownState = new AnimationState();
}
