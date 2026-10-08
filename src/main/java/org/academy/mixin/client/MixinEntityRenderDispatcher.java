package org.academy.mixin.client;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.academy.internal.client.ability.mentalout.MentalIntrusionClientState;
import org.academy.internal.client.ability.teleport.ChunkLeapGodView;
import org.academy.internal.client.renderer.entity.layers.CloudroomLayer;
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
        // 26.2 的 LevelExtractor.isEntityVisible 额外要求实体所在方块区块已编译可见，
        // 云室(异常能力透视)目标依赖该处主动放行；1.21.1 无 LevelExtractor，
        // 此处并入 1.21.1 的可见性门 (shouldRender) 以保留云室透视。
        if (entity instanceof LivingEntity living
                && CloudroomLayer.shouldReveal(living)
                && culler.isVisible(entity.getBoundingBox())) {
            cir.setReturnValue(true);
            return;
        }
        // God view looks down from outside the world; drawing the player from there is an obstruction.
        if (ChunkLeapGodView.isActive() && entity == net.minecraft.client.Minecraft.getInstance().player) {
            cir.setReturnValue(false);
        }
    }
}
