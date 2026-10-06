package org.academy.api.client.render;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;

import java.util.ArrayList;
import java.util.List;

/**
 * 1.21.1 原生管线抽象, 移植自 26.2 `com.mojang.blaze3d.pipeline.RenderPipeline`.
 *
 * 迁自 Kotlin 源文件 RenderPipeline.kt. 保留原有用例成员:
 * - vertexFormatBindings: 槽位 -> 顶点格式 (单缓冲区时长度为 1).
 * - primitiveTopology: 顶点模式 (VertexFormat.Mode).
 * - getVertexFormatBinding/activeSlots 沿用原访问方式.
 * - renderType: 1.21.1 即时渲染对应的 RenderType.
 */
public final class RenderPipeline {
    private final String name;
    private final List<VertexFormat> vertexFormatBindings;
    private final VertexFormat.Mode primitiveTopology;
    private final RenderType renderType;

    public RenderPipeline(
            String name,
            List<VertexFormat> vertexFormatBindings,
            VertexFormat.Mode primitiveTopology,
            RenderType renderType
    ) {
        this.name = name;
        this.vertexFormatBindings = vertexFormatBindings;
        this.primitiveTopology = primitiveTopology;
        this.renderType = renderType;
    }

    public String getName() {
        return name;
    }

    public List<VertexFormat> getVertexFormatBindings() {
        return vertexFormatBindings;
    }

    public VertexFormat.Mode getPrimitiveTopology() {
        return primitiveTopology;
    }

    public RenderType getRenderType() {
        return renderType;
    }

    public VertexFormat getVertexFormatBinding(int slot) {
        return slot >= 0 && slot < vertexFormatBindings.size() ? vertexFormatBindings.get(slot) : null;
    }

    public List<Integer> getActiveSlots() {
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < vertexFormatBindings.size(); i++) {
            if (vertexFormatBindings.get(i) != null) {
                result.add(i);
            }
        }
        return result;
    }

    public boolean getConnectedPrimitives() {
        return primitiveTopology.connectedPrimitives;
    }

    @Override
    public String toString() {
        return "RenderPipeline(" + name + ")";
    }
}
