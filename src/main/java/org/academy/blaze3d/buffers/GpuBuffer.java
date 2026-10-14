package org.academy.blaze3d.buffers;

import java.nio.ByteBuffer;
import java.util.function.Supplier;

/**
 * 移植自 Minecraft 26.2 的 {@code com.mojang.blaze3d.buffers.GpuBuffer}.
 *
 * <p>26.2 以 {@code GpuBuffer} 描述 GPU 缓冲对象 (uniform/顶点/索引等), 由
 * {@code GpuDevice.createBuffer(...)} 创建, 供命令编码器写入, 渲染时以 {@code slice()} 切片提交.
 * 1.21.1 无该对象; 本垫层按仓库 C-stun 惯例保符号: 持有一份 CPU {@link ByteBuffer} 作为切片内容.
 */
public final class GpuBuffer {
    /** 用途位: 作 uniform 缓冲区 (C-stun 下仅为符号). */
    public static final int USAGE_UNIFORM = 1;
    /** 用途位: 作为复制目标 (C-stun 下仅为符号). */
    public static final int USAGE_COPY_DST = 2;
    /** 用途位: 作顶点缓冲区 (C-stun 下仅为符号). */
    public static final int USAGE_VERTEX = 4;
    /** 用途位: 作索引缓冲区 (C-stun 下仅为符号). */
    public static final int USAGE_INDEX = 8;

    private final ByteBuffer contents;
    private final boolean closed;

    private GpuBuffer(ByteBuffer contents) {
        this.contents = contents;
        this.closed = false;
    }

    /** 返回整块的 CPU 切片 (1.21.1 下命令编码器把数据写入该切片头部). */
    public ByteBuffer slice() {
        return contents.duplicate();
    }

    /** 释放缓冲对象. 1.21.1 无 GPU 显存, 为空操作. */
    public void close() {
        // C-stun.
    }

    public boolean isClosed() {
        return closed;
    }

    /** 工厂: 由设备按大小创建 CPU 侧缓冲. 名称仅在日志中出现, 不分配显存. */
    public static GpuBuffer create(Supplier<String> name, int usage, long size) {
        int capacity;
        if (size <= 0) {
            capacity = 0;
        } else {
            capacity = (int) Math.min(size, Integer.MAX_VALUE);
        }
        return new GpuBuffer(ByteBuffer.allocate(capacity));
    }
}
