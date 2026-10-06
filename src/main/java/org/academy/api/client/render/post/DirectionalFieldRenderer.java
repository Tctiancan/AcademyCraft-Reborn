package org.academy.api.client.render.post;

import net.minecraft.world.phys.Vec3;
import org.academy.api.common.vfx.DirectionalArea;
import org.joml.Matrix4fc;

import java.util.List;

/**
 * 1.21.1 简化版 (C-stun)。
 *
 * 26.2 通过命令编码器 (CommandEncoder/RenderPass) + UBO 完成屏幕空间方向场散射后处理;
 * 1.21.1 无对应 API, 且该渲染位于世界级 VFX 链路上。按首版简化策略整体跳过,
 * 保留对外方法签名 (render/renderedFrames/close) 供上层管线调用, 内部为空操作。
 */
public final class DirectionalFieldRenderer {
    private DirectionalFieldRenderer() {
    }

    public static long renderedFrames() {
        return 0;
    }

    public static void render(Vec3 camera, Matrix4fc view, Matrix4fc projection, List<DirectionalArea> areas) {
        // C-stun: 1.21.1 不执行方向场散射后处理。
        close();
    }

    public static void close() {
    }
}
