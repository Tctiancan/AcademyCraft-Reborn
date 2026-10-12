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
     * {@code mcCamera.rotation()} 给出, 世界到相机空间的视旋转取其共轭。
     *
     * <p>投影矩阵不能置空: {@link org.academy.internal.client.render.vfx.VfxVisibility} 的
     * 视锥剔除会用它构造 {@code projectionView} 判定特效是否可见, 单位阵会让 near 平面塌缩为
     * {@code z=0}, 令略远处的特效 (如第三人称下的 Dark matter cut 斩击) 被误判为不可见。实际
     * 渲染时渲染器自管 {@code RenderSystem} 当前投影, 不读取该矩阵, 因此这里给出正常透视投影
     * 仅用于正确的视锥剔除。
     */
    public static void submit(DeltaTracker deltaTracker, Camera mcCamera) {
        var partialTick = deltaTracker.getGameTimeDeltaPartialTick(false);
        var level = Minecraft.getInstance().level;
        var gameTime = level == null ? 0.0f : level.getGameTime() + partialTick;
        var pos = mcCamera.getPosition();
        var orientation = new Quaternionf(mcCamera.rotation());
        var window = Minecraft.getInstance().getWindow();
        float aspect = (float) window.getWidth() / Math.max(1.0f, (float) window.getHeight());
        var projection = new Matrix4f().setPerspective((float) Math.toRadians(70.0), aspect, 0.05f, 1024.0f);
        var camera = new VfxCamera(
                new Vector3f((float) pos.x, (float) pos.y, (float) pos.z),
                orientation,
                projection,
                new Matrix4f().rotation(new Quaternionf(orientation).conjugate()),
                70.0f,
                null
        );
        VfxManager.INSTANCE.sampleFrame(new VfxFrameContext(
                camera, deltaTracker.getGameTimeDeltaTicks(), gameTime, partialTick
        ));
    }
}
