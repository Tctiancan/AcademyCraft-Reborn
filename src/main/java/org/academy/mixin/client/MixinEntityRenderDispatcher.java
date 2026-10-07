package org.academy.mixin.client;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import org.academy.internal.client.ability.mentalout.MentalIntrusionClientState;
import org.academy.internal.client.ability.teleport.ChunkLeapGodView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderDispatcher.class)
public abstract class MixinEntityRenderDispatcher {
    // 1.21.1 无 26.2 的 EntityRenderDispatcher.submit(EntityRenderState, CameraRenderState, ...)。
    // 原 academy$captureSurfaceEntity 在提交实体时向 WorldSurfaceMasks 捕获表面(配合异常空间斩切后处理)。
    // 该 API 依赖 26.2 的 render-state 体系(EntityRenderState/CameraRenderState)与节点化提交,
    // 1.21.1 需改用 EntityRenderDispatcher.render 即时渲染路径适配,此处待定集成。
    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private <E extends Entity> void academy$hideMentalPerceptionTarget(
            E entity,
            Frustum culler,
            double camX,
            double camY,
            double camZ,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (MentalIntrusionClientState.isHidden(entity)) cir.setReturnValue(false);
        // God view looks down from outside the world; drawing the player from there is an obstruction.
        if (ChunkLeapGodView.isActive() && entity == net.minecraft.client.Minecraft.getInstance().player) {
            cir.setReturnValue(false);
        }
    }
}
