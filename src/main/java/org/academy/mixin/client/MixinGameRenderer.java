package org.academy.mixin.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.neoforged.neoforge.common.NeoForge;
import org.academy.api.client.gui.animation.AnimationManager;
import org.academy.api.client.gui.frame.UiFrame;
import org.academy.api.client.render.Render;
import org.academy.api.client.vanilla.RenderLoopEvent;
import org.academy.internal.client.render.vfx.WingAvatarRegistry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 移植自 Minecraft 26.2 模组渲染后端, 适配 1.21.1.
 *
 * 迁移策略 (C-stun 世界级 VFX):
 * - 26.2 GameRenderer 通过 FeatureRenderDispatcher/SubmitNodeStorage/CameraRenderState 的节点化
 *   延迟提交管线处理世界级 VFX (SpatialCutFrameProjectionContext/WorldLineOverlayPass) 与
 *   第一人称特效 (WingAvatarRegistry/Electromaster/LightShield)。1.21.1 无该节点化提交管线。
 * - 按首版简化策略, 保留可移植的每帧 RenderLoopEvent 触发点与资源释放钩子; 世界级 VFX 与
 *   第一人称特效挂点清空, 待后续以 1.21.1 RenderHandEvent/RenderLevelStageEvent 重建。
 */
@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "render", at = @At("HEAD"))
    private void onFrameUpdate(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {
        // 26.2 MixinGameRenderer 每帧清空 wing avatar capture (PlayerModel/humanoid render 期间填充),
        // 避免第三人称捕获的相机相对根矩阵残留到下一帧: 否则本地玩家从第三人称切到第一人称后,
        // 遍历仍命中断掉的陈旧 capture → 黑翼按旧第三人称根定位而离体。
        WingAvatarRegistry.beginFrame();
        AnimationManager.INSTANCE.onFrameUpdate();
        UiFrame.INSTANCE.onFrame();
        NeoForge.EVENT_BUS.post(new RenderLoopEvent());
    }

    @Inject(method = "close", at = @At("HEAD"))
    private void onClose(CallbackInfo ci) {
        Render.close();
    }
}
