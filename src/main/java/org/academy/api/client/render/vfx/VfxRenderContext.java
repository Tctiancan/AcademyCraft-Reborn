package org.academy.api.client.render.vfx;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * 1.21.1 简化版 (C-stun)。
 *
 * 26.2 该上下文向渲染器暴露 GPU 资源 (device/projectionUniform/mainColor/mainDepth/
 * bloomInput* / sceneColor); 1.21.1 无对应 API, 且 VFX 渲染已整体桩化。按首版简化策略,
 * 仅保留类型无关的帧数据访问 (gameTime/cameraPos/cameraOrientation/viewRotationMatrix)。
 */
public interface VfxRenderContext {
    float gameTime();

    Vector3f cameraPos();

    Quaternionf cameraOrientation();

    /**
     * Camera-relative world-to-view rotation captured for this VFX frame.
     */
    default Matrix4f viewRotationMatrix() {
        return new Matrix4f().rotation(cameraOrientation());
    }
}
