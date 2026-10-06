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
     * C-stun: 26.2 {@code CameraRenderState}/{@code RenderSystem.getProjectionMatrixBuffer()} 世界渲染
     * 上下文在 1.21.1 暂不移植。改用原版相机数据构建 {@link VfxCamera}，GPU 投影 uniform 置空。
     */
    public static void submit(DeltaTracker deltaTracker, Camera mcCamera) {
        var partialTick = deltaTracker.getGameTimeDeltaPartialTick(false);
        var level = Minecraft.getInstance().level;
        var gameTime = level == null ? 0.0f : level.getGameTime() + partialTick;
        var pos = mcCamera.getPosition();
        var camera = new VfxCamera(
                new Vector3f((float) pos.x, (float) pos.y, (float) pos.z),
                new Quaternionf(mcCamera.rotation()),
                new Matrix4f(),
                new Matrix4f(),
                70.0f,
                null
        );
        VfxManager.INSTANCE.sampleFrame(new VfxFrameContext(
                camera, deltaTracker.getGameTimeDeltaTicks(), gameTime, partialTick
        ));
    }
}
