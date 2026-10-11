package org.academy.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.neoforged.neoforge.common.NeoForge;
import org.academy.api.client.render.LevelRenderEvent;
import org.academy.api.client.render.MatrixStack;
import org.academy.api.client.render.post.DirectionalFieldRenderer;
import org.academy.api.client.render.post.GlowEffect;
import org.academy.api.client.render.post.PostEffect;
import org.academy.api.client.render.post.WorldSurfaceMasks;
import org.academy.api.client.render.vfx.VfxContexts;
import org.academy.api.client.render.vfx.VfxManager;
import org.academy.api.client.renderer.RendererManager;
import org.academy.internal.client.ability.aeromanip.FlowSenseClient;
import org.academy.internal.client.ability.mentalout.WideAreaInterferenceClientState;
import org.academy.internal.client.render.vfx.SpacialExcisionVfxClient;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class MixinLevelRenderer {
    @Inject(method = "renderLevel", at = @At("HEAD"))
    private void academy$beginDirectionalFields(
            DeltaTracker deltaTracker, boolean renderBlockOutline, Camera camera,
            net.minecraft.client.renderer.GameRenderer gameRenderer,
            net.minecraft.client.renderer.LightTexture lightTexture,
            Matrix4f frustumMatrix, Matrix4f projectionMatrix,
            CallbackInfo ci
    ) {
        org.academy.internal.client.render.vfx.InterferenceFieldClient.beginFrame();
    }

    /**
     * 1.21.1 适配：26.2 的 `LevelRenderer.render(GraphicsResourceAllocator, DeltaTracker, boolean,
     * CameraRenderState, GpuBufferSlice, Vector4f, boolean)` 与 `submitEntities(PoseStack,
     * LevelRenderState, SubmitNodeCollector)` 均依赖 1.21.1 不存在的渲染状态与节点收集器。
     * 改为挂在 1.21.1 `renderLevel(DeltaTracker, boolean, Camera, GameRenderer, LightTexture,
     * Matrix4f, Matrix4f)` 的 RETURN：
     * - cameraState.pos/viewRotationMatrix/projectionMatrix → `Camera.getPosition()` + 两个矩阵参数；
     * - `mainRenderTarget()` → `getMainRenderTarget()`；`Minecraft.getDeltaTracker()` → 方法参数；
     * - 26.2 经 `VfxGraphManager.renderFrame(GpuTextureView…)` 把 shader 图渲染到主帧缓冲颜色/深度纹理
     *   视图的后处理，其 `GpuTextureView` 无 1.21.1 等价物，C-stun；
     * - 26.2 经 `submitEntities` 强制 `shouldShowEntityOutlines` 与 `SubmitNodeCollector` 世界几何提交的
     *   行为无 1.21.1 对应节点流，跳过；`LevelRenderEvent` 改在此处以 1.21.1 四参构造触发。
     */
    @Inject(
            order = Integer.MAX_VALUE,
            method = "renderLevel",
            at = @At("RETURN")
    )
    private void renderLevel(
            DeltaTracker deltaTracker, boolean renderBlockOutline, Camera camera,
            net.minecraft.client.renderer.GameRenderer gameRenderer,
            net.minecraft.client.renderer.LightTexture lightTexture,
            Matrix4f frustumMatrix, Matrix4f projectionMatrix,
            CallbackInfo ci
    ) {
        var viewRotation = new Matrix4f(frustumMatrix);
        var pos = camera.getPosition();
        WideAreaInterferenceClientState.captureRenderCamera(pos, viewRotation, projectionMatrix);
        VfxContexts.submit(deltaTracker, camera);
        VfxManager.INSTANCE.renderFrame();
        SpacialExcisionVfxClient.prepareSourceValidation();
        PostEffect.pre();
        SpacialExcisionVfxClient.renderPost();
        WorldSurfaceMasks.renderEntities();
        DirectionalFieldRenderer.render(pos, viewRotation, projectionMatrix,
                org.academy.internal.client.render.vfx.InterferenceFieldClient.areas());
        // 1.21.1 移植: 26.2 的相位几何经 `MATRICES...` shader 应用 modelViewProjection 把相机相对
        // 坐标映射到裁剪空间 (模型视图为相机旋转矩阵)。移植版 Phase.draw() 用 position_color, 其顶点
        // 同样经 `ProjMat * ModelViewMat * pos` 变换, 但此前无人设置模型视图, 相机相对坐标在错误的
        // 矩阵下被裁剪到屏幕外而导致相位几何不可见。此处临时将模型视图置为相机旋转 (投影在 renderLevel
        // RETURN 时仍为等级投影, 无需改动), 冲刷辉光阶段后再还原, 避免影响其后 (FlowSense/手部渲染)
        // 的渲染状态。
        // 相机相对坐标 (world - camPos) 与 Frustum 用同一约定: 相机空间 = viewRotation * (相机相对坐标),
        // 其中 viewRotation(=renderLevel 的 frustumMatrix) 已被 Frustum.calculateFrustum 证实就是
        // world→camera 的 view 旋转 (clip = projection * frustumMatrix * cameraRelative). 故模型视图
        // 直接置为 viewRotation, 无需取逆; 投影在 renderLevel RETURN 时仍是等级投影, 无需改动.
        // 冲刷前强制恢复等级透视投影: VfxManager.renderFrame()/WorldSurfaceMasks 等前置调用可能
        // 改过投影未还原, 若用遗留投影, 相机相对坐标会被错误裁剪而不可见。投影与 GameRenderer
        // 的 resetProjectionMatrix 完全一致 (DISTANCE_TO_ORIGIN). 之后画完还原 modelview/projection.
        var prevProjection = new Matrix4f(RenderSystem.getProjectionMatrix());
        RenderSystem.setProjectionMatrix(projectionMatrix, com.mojang.blaze3d.vertex.VertexSorting.DISTANCE_TO_ORIGIN);
        var modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushMatrix();
        modelViewStack.set(viewRotation);
        RenderSystem.applyModelViewMatrix();
        try {
            // 扭曲光环 (动能冲击波) 须采样主帧缓冲场景作径向偏移, 需离屏只读快照; 在模型视图/
            // 投影窗口内冲刷, 其四边形几何方经正确的 ProjMat*ModelViewMat 变换映射到屏幕环带。
            PostEffect.drawPre();
            GlowEffect.getInstance().process();
        } finally {
            modelViewStack.popMatrix();
            RenderSystem.applyModelViewMatrix();
            RenderSystem.setProjectionMatrix(prevProjection, com.mojang.blaze3d.vertex.VertexSorting.DISTANCE_TO_ORIGIN);
        }
        PostEffect.post();
        FlowSenseClient.render(pos, viewRotation, projectionMatrix);

        var partialTick = deltaTracker.getGameTimeDeltaPartialTick(false);
        var poseStack = new PoseStack();
        poseStack.last().pose().set(frustumMatrix);
        var event = new LevelRenderEvent(
                partialTick,
                new MatrixStack().setFrom(poseStack.last()),
                poseStack,
                camera.getPosition()
        );
        NeoForge.EVENT_BUS.post(event);
        renderFirstPersonEffectsWithHiddenHud(deltaTracker, poseStack);
    }

    private void renderFirstPersonEffectsWithHiddenHud(DeltaTracker deltaTracker, PoseStack poseStack) {
        var minecraft = Minecraft.getInstance();
        // 仅当 GUI 被隐藏 (F1) 时才复用 26.2 的 hidden-HUD 第一人称特效挂点:
        // 此分支在第 level render 相机视图 poseStack (frustumMatrix) 上渲染
        // renderFirstPersonWhenHudHidden()==true 的桥接渲染器 (电磁铁武器),
        // 因为此时 renderItemInHand 不会再绘制手部与普通第一人称特效。
        if (!minecraft.options.hideGui
                || !minecraft.options.getCameraType().isFirstPerson()
                || minecraft.gameMode == null) {
            return;
        }
        var gameType = minecraft.gameMode.getPlayerMode();
        if (gameType == net.minecraft.world.level.GameType.SPECTATOR) return;
        var player = minecraft.player;
        if (player == null) return;
        var partialTick = deltaTracker.getGameTimeDeltaPartialTick(false);
        var bufferSource = minecraft.renderBuffers().bufferSource();
        var packedLight = minecraft.getEntityRenderDispatcher().getPackedLightCoords(player, partialTick);
        RendererManager.renderEffectFirstPersonWithHiddenHud(
                poseStack, bufferSource, player, packedLight, partialTick);
        bufferSource.endBatch();
    }
}
