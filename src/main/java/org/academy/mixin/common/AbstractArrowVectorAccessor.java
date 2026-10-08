package org.academy.mixin.common;

import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractArrow.class)
public interface AbstractArrowVectorAccessor {
    @Accessor("inGround")
    boolean academy$isInGround();

    @Invoker("tryPickup")
    boolean academy$tryPickup(Player player);

    @Accessor("inGround")
    void academy$setInGround(boolean inGround);

    @Accessor("life")
    void academy$setLife(int life);

    @Accessor("inGroundTime")
    void academy$setInGroundTime(int inGroundTime);

    @Invoker("resetPiercedEntities")
    void academy$resetPiercedEntities();
}
