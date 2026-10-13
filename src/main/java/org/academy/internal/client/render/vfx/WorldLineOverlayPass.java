package org.academy.internal.client.render.vfx;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import org.academy.AcademyCraft;
import org.academy.api.client.render.LevelRenderEvent;
import org.academy.api.client.render.MatrixStack;
import org.academy.api.client.render.Render;
import org.joml.Matrix4fc;
import org.slf4j.Logger;

import java.util.function.BiConsumer;

/**
 * 1.21.1 简化版 (C-stun)。
 *
 * 26.2 通过 SubmitNodeCollector/SubmitNodeStorage/FeatureRenderDispatcher 的节点化
 * "shader-managed world pass" 叠加世界空间叠加层 (探测线/传送光标), 在 1.21.1 均不存在。
 * 这里保留该独立叠加层的语义: 世界探测器 / 传送等线框属于 "贴着地面的可穿墙提示", 必须
 * 在世界 pass 之后以 深度测试关闭 覆盖绘制, 否则会被地形/方块挡住而不可见。
 * 因此在 1.21.1 上将其实现为: 识别 [Render.RenderTypes.MINE_DETECT_LINES] 即时提交到主渲染
 * 缓冲, 绘制期间显式关闭深度测试 (并在结束后恢复), 使轮廓始终可见、可穿墙。
 */
public final class WorldLineOverlayPass {
    private static final Logger LOGGER = AcademyCraft.getLogger();

    private WorldLineOverlayPass() {
    }

    public static void beginFrame(ClientLevel level) {
        // C-stun: 1.21.1 不维护延迟提交队列。
    }

    public static boolean accepts(RenderType renderType) {
        return renderType == Render.RenderTypes.MINE_DETECT_LINES
                || renderType == Render.RenderTypes.TELEPORT_CURSOR;
    }

    public static void submit(PoseStack poseStack, RenderType renderType, MatrixStack matrixStack,
                              BiConsumer<MatrixStack, VertexConsumer> renderer) {
        var bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        var vertexConsumer = bufferSource.getBuffer(renderType);
        // 关闭深度测试, 使叠加线框不被方块遮挡; 结束后必须恢复, 以免影响后续渲染。
        RenderSystem.disableDepthTest();
        try {
            renderer.accept(matrixStack, vertexConsumer);
            bufferSource.endBatch(renderType);
        } finally {
            RenderSystem.enableDepthTest();
        }
    }

    public static void submitPose(PoseStack poseStack, RenderType renderType,
                                  LevelRenderEvent.CustomGeometryRenderer renderer) {
        var bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        var vertexConsumer = bufferSource.getBuffer(renderType);
        RenderSystem.disableDepthTest();
        try {
            renderer.render(poseStack, vertexConsumer);
            bufferSource.endBatch(renderType);
        } finally {
            RenderSystem.enableDepthTest();
        }
    }

    public static void renderWorld(Matrix4fc modelViewMatrix) {
        // C-stun: 1.21.1 不执行叠加 pass。
    }

    public static void clear() {
    }
}
