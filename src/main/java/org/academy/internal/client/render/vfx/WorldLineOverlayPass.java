package org.academy.internal.client.render.vfx;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import org.academy.AcademyCraft;
import org.academy.api.client.render.LevelRenderEvent;
import org.academy.api.client.render.MatrixStack;
import org.joml.Matrix4fc;
import org.slf4j.Logger;

import java.util.function.BiConsumer;

/**
 * 1.21.1 简化版 (C-stun)。
 *
 * 26.2 通过 SubmitNodeCollector/SubmitNodeStorage/FeatureRenderDispatcher 的节点化
 * "shader-managed world pass" 叠加世界空间叠加层 (探测线/传送光标), 在 1.21.1 均不存在。
 * 按首版简化策略整体跳过该独立 pass: 保留 beginFrame/accepts/submit/submitPose/renderWorld/
 * clear 签名, 全部为空操作。世界叠加层改由各技能经 LevelRenderEvent 即时渲染提交。
 */
public final class WorldLineOverlayPass {
    private static final Logger LOGGER = AcademyCraft.getLogger();

    private WorldLineOverlayPass() {
    }

    public static void beginFrame(ClientLevel level) {
        // C-stun: 1.21.1 不维护延迟提交队列。
    }

    public static boolean accepts(RenderType renderType) {
        return false;
    }

    public static void submit(PoseStack poseStack, RenderType renderType, MatrixStack matrixStack,
                              BiConsumer<MatrixStack, VertexConsumer> renderer) {
        // C-stun: 1.21.1 不延迟提交。
    }

    public static void submitPose(PoseStack poseStack, RenderType renderType,
                                  LevelRenderEvent.CustomGeometryRenderer renderer) {
        // C-stun: 1.21.1 不延迟提交。
    }

    public static void renderWorld(Matrix4fc modelViewMatrix) {
        // C-stun: 1.21.1 不执行叠加 pass。
    }

    public static void clear() {
    }
}
