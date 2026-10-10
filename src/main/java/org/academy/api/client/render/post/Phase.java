package org.academy.api.client.render.post;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
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
 * 当前绑定的帧缓冲 (由 {@link PostEffect#pre()} 切换为离屏目标 {@code MAIN_SCENE}), 再清空缓冲
 * 供下一帧复用。
 *
 * <p>每个 {@code Phase} 拥有私有 {@link ByteBufferBuilder}/{@link BufferBuilder}, 绝不借用全局
 * {@link com.mojang.blaze3d.vertex.Tesselator} 的共享缓冲区。世界渲染在帧内会在共享缓冲上反复
 * {@code begin/build/clear}, 若阶段跨帧持有该共享缓冲, 其游标被推进或区间被废弃, 会令跨帧累加的
 * 顶点失效并在 {@code build()} 上抛 "Not building!"。私有分配器保证阶段几何在整帧到
 * {@link #draw()} 之前始终有效。格式保持 {@code POSITION_COLOR}/{@code TRIANGLES}。
 */
public class Phase {
    private static final int INITIAL_CAPACITY = 786432;
    private final ByteBufferBuilder allocator;
    private BufferBuilder builder;

    public Phase(String name) {
        this.allocator = new ByteBufferBuilder(INITIAL_CAPACITY);
        this.builder = newBuffer();
    }

    private BufferBuilder newBuffer() {
        return new BufferBuilder(allocator, VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
    }

    public VertexConsumer getBuffer(RenderType renderType) {
        return builder;
    }

    public void draw() {
        MeshData meshdata = this.builder.build();
        if (meshdata != null) {
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            BufferUploader.draw(meshdata);
        }
        this.allocator.clear();
        this.builder = newBuffer();
    }

    public void close() {
        this.allocator.close();
    }
}
