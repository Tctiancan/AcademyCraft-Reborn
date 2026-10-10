package org.academy.api.client.render.post;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;

/**
 * 1.21.1 实装 (原 26.2 后处理阶段缓冲)。
 *
 * <p>26.2 通过 {@code StagedVertexBuffer} + {@code RenderType.drawFromBuffer} 延迟提交阶段顶点;
 * 1.21.1 无对应 API。本实现以即时态缓冲等价物保持同一调用形态 ({@link #getBuffer(RenderType)} 返回
 * 顶点消费者), {@link #draw()} 把阶段内累积的三角形列表以 {@code position_color} shader 冲刷进
 * 当前绑定的帧缓冲 (由 {@link PostEffect#pre()} 切换为离屏目标 {@code MAIN_SCENE}), 再为重开下一帧
 * 重建缓冲。格式保持首版选定的 {@code POSITION_COLOR}/{@code TRIANGLES}。
 */
public class Phase {
    private BufferBuilder builder;

    public Phase(String name) {
        this.builder = Tesselator.getInstance().begin(
                VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
    }

    public VertexConsumer getBuffer(RenderType renderType) {
        return builder;
    }

    public void draw() {
        MeshData meshdata = this.builder.build();
        if (meshdata == null) {
            return;
        }
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferUploader.draw(meshdata);
        this.builder = Tesselator.getInstance().begin(
                VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
    }

    public void close() {
    }
}
