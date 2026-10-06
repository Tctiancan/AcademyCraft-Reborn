package org.academy.api.client.render;

import java.nio.ByteBuffer;

/**
 * 移植自 26.2 `UniformBinding` (持 GpuBufferSlice).
 * 1.21.1 无 GPU UBO, 改为持有已序列化的 [ByteBuffer] 数据; 上层自研 shader 在需要时消费.
 * 首版即时渲染未接入 UBO, 该数据保留以便后续步骤使用.
 */
public record UniformBinding(String name, ByteBuffer data) {
}
