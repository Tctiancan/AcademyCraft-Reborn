package org.academy.api.client.render.post;

import org.academy.api.common.vfx.DirectionalArea;

import java.util.List;

/**
 * 1.21.1 简化版 (C-stun)。
 *
 * 26.2 通过新世界渲染内部构件 (FeatureRenderDispatcher/ChunkSectionsToRender/
 * OutputTarget/PreparedRenderType/TextureTarget) 拾取材质与几何遮罩; 1.21.1 无对应 API,
 * 且该链路位于世界级 VFX。按首版简化策略整体跳过, 保留供上层 (InterferenceFieldClient、
 * MixinLevelRenderer) 调用的类型无关方法 (active/ready/beginFrame/reset/close/renderEntities),
 * 其余 GPU 相关方法与返回 26.2 类型的访问器一并移除。
 */
public final class WorldSurfaceMasks {
    private static boolean ready;
    private static List<DirectionalArea> areas = List.of();

    private WorldSurfaceMasks() {
    }

    public static boolean active() {
        return !areas.isEmpty();
    }

    public static boolean ready() {
        return ready;
    }

    public static void beginFrame(List<DirectionalArea> regions) {
        areas = regions;
        ready = false;
    }

    public static void reset() {
        areas = List.of();
        ready = false;
    }

    public static void close() {
        reset();
    }

    public static void renderEntities() {
        // C-stun: 1.21.1 不重放实体遮罩。
    }
}
