package org.academy.api.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.Event;
import org.academy.internal.client.render.vfx.WorldLineOverlayPass;

import java.util.function.BiConsumer;

public class LevelRenderEvent extends Event {
    /**
     * 坐标系原点是相机位置, 而非世界原点
     */
    private final MatrixStack matrixStack;
    private final float partialTick;
    private final PoseStack poseStack;
    private final Vec3 cameraPosition;

    public LevelRenderEvent(float partialTick, MatrixStack matrixStack, PoseStack poseStack,
                            Vec3 cameraPosition) {
        this.matrixStack = matrixStack;
        this.partialTick = partialTick;
        this.poseStack = poseStack;
        this.cameraPosition = cameraPosition;
    }

    public MatrixStack getMatrixStack() {
        return matrixStack;
    }

    public float getPartialTick() {
        return partialTick;
    }

    /** Exact world-space camera position captured for this render state. */
    public Vec3 getCameraPosition() {
        return cameraPosition;
    }

    /**
     * 1.21.1 即时渲染: 替代 26.2 SubmitNodeCollector.submitCustomGeometry 的延迟提交。
     */
    public void submitCustomGeometry(RenderType renderType,
                                     BiConsumer<MatrixStack, VertexConsumer> renderer) {
        var snapshot = matrixStack.copy();
        if (WorldLineOverlayPass.accepts(renderType)) {
            WorldLineOverlayPass.submit(poseStack, renderType, snapshot, renderer);
            return;
        }
        var bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        var vertexConsumer = bufferSource.getBuffer(renderType);
        renderer.accept(snapshot, vertexConsumer);
        bufferSource.endBatch(renderType);
    }

    public void submitPoseGeometry(RenderType renderType,
                                   CustomGeometryRenderer renderer) {
        if (WorldLineOverlayPass.accepts(renderType)) {
            WorldLineOverlayPass.submitPose(poseStack, renderType, renderer);
            return;
        }
        var bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        var vertexConsumer = bufferSource.getBuffer(renderType);
        renderer.render(poseStack, vertexConsumer);
        bufferSource.endBatch(renderType);
    }

    @FunctionalInterface
    public interface CustomGeometryRenderer {
        void render(PoseStack poseStack, VertexConsumer vertexConsumer);
    }
}
