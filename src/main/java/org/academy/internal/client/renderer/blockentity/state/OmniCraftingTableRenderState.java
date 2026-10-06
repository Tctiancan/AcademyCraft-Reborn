package org.academy.internal.client.renderer.blockentity.state;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.AnimationState;

public final class OmniCraftingTableRenderState {
    public float ageInTicks;
    public AnimationState unfoldingState = new AnimationState();
    public boolean isMain;
    public Direction facing = Direction.NORTH;
}
