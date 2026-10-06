package org.academy.internal.client.renderer.blockentity.state;

import net.minecraft.world.entity.AnimationState;

public final class WirelessNodeRenderState {
    public float ageInTicks;
    public AnimationState coreState = new AnimationState();
    public int connectedUsersCount;
    public int maxConnectedUsers;
}
