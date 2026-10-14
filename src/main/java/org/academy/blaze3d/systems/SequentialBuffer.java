package org.academy.blaze3d.systems;

import org.academy.blaze3d.PrimitiveTopology;

import java.nio.ByteBuffer;

/**
 * 移植自 Minecraft 26.2 的 {@code com.mojang.blaze3d.systems.SequentialBuffer}.
 *
 * <p>26.2 以 {@code SequentialBuffer} 描述按需生成 `0,1,2,…` 连续索引序列的索引缓冲, 由
 * {@code RenderSystem.getSequentialBuffer(topology)} 获取. 1.21.1 无该对象; 本垫层按仓库
 * C-stun 惯例保符号: {@link #getBuffer(int)} 返回空切片, {@link #type()} 返回创建时拓扑.
 */
public final class SequentialBuffer {
    private final ByteBuffer empty = ByteBuffer.allocate(0);
    private final PrimitiveTopology type;

    private SequentialBuffer(PrimitiveTopology type) {
        this.type = type;
    }

    /** 由拓扑构造共享缓存实例 (C-stun 下无 GPU 索引缓冲). */
    public static SequentialBuffer of(PrimitiveTopology type) {
        return new SequentialBuffer(type);
    }

    /** 返回覆盖给定顶点数的连续索引缓冲; 1.21.1 下返回空切片. */
    public ByteBuffer getBuffer(int vertexCount) {
        return empty.duplicate();
    }

    /** 返回创建时的图元拓扑. */
    public PrimitiveTopology type() {
        return type;
    }
}
