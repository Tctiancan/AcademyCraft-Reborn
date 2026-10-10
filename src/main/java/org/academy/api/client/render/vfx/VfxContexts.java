package org.academy.api.client.render.vfx;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class VfxContexts {
    private VfxContexts() {
    }

    /**
     * 26.2 {@code CameraRenderState}/{@code RenderSystem.getProjectionMatrixBuffer()} 世界渲染
     * 上下文在 1.21.1 暂不移植。改用原版相机数据构建 {@link VfxCamera}: 相机朝向由
     * {@code mcCamera.rotation()} 给出, 世界到相机空间的视旋转取其共轭; GPU 投影 uniform 置空
     * (由渲染时 {@code RenderSystem} 当前投影矩阵提供)。
     */
    public static void submit(DeltaTracker deltaTracker, Camera mcCamera) {
        var partialTick = deltaTracker.getGameTimeDeltaPartialTick(false);
        var level = Minecraft.getInstance().level;
        var gameTime = level == null ? 0.0f : level.getGameTime() + partialTick;
        var pos = mcCamera.getPosition();
        var orientation = new Quaternionf(mcCamera.rotation());
        var camera = new VfxCamera(
                new Vector3f((float) pos.x, (float) pos.y, (float) pos.z),
                orientation,
                new Matrix4f(),
                new Matrix4f().rotation(new Quaternionf(orientation).conjugate()),
                70.0f,
                null
        );
        VfxManager.INSTANCE.sampleFrame(new VfxFrameContext(
                camera, deltaTracker.getGameTimeDeltaTicks(), gameTime, partialTick
        ));
    }
}
