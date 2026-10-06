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
        ByteBuffer buffer = ByteBuffer.allocate(size).order(java.nio.ByteOrder.nativeOrder());
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
