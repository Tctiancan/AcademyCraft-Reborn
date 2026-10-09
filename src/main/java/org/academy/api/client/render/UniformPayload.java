package org.academy.api.client.render;

import java.nio.ByteBuffer;

/**
 * 移植自 26.2 `UniformPayload` (依赖 DynamicUniformStorage.DynamicUniform).
 * 1.21.1 改依赖模组内 [DynamicUniform] 接口, 保留数据写入契约.
 */
public record UniformPayload<T extends DynamicUniform>(
        String name, Class<T> type, T data, int size
) {
    /**
     * 把 payload 序列化为独立 [ByteBuffer] (native 字节序), 供 1.21.1 使用.
     */
    public ByteBuffer toByteBuffer() {
        // 必须使用 direct 缓冲: JOML 的 Vector*.get(ByteBuffer) 走 Unsafe 快径,
        // 会读取 ByteBuffer.address 字段作为写入地址; 堆缓冲该字段恒为 0, 会导致写到地址 0 处而原生崩溃.
        ByteBuffer buffer = ByteBuffer.allocateDirect(size).order(java.nio.ByteOrder.nativeOrder());
        data.write(buffer);
        buffer.rewind();
        return buffer;
    }

    /**
     * Kotlin 从 Java record 构造此类时, 对嵌套数据类型 (如 `BlendQuadWidget.SDFData`) 泛型推断失败
     * (Cannot infer type for T). 提供静态工厂以显式绑定 {@code T}, 使 Kotlin 侧可安全调用.
     */
    public static <T extends DynamicUniform> UniformPayload<T> of(String name, Class<T> type, T data, int size) {
        return new UniformPayload<>(name, type, data, size);
    }
}
