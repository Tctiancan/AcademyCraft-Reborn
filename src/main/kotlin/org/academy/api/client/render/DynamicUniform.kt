package org.academy.api.client.render

import java.nio.ByteBuffer

/**
 * 移植自 26.2 `net.minecraft.client.renderer.DynamicUniformStorage.DynamicUniform`.
 * 1.21.1 没有 UBO/动态 uniform 后端, 此处保留其数据写入契约以便 uniform 数据类沿用.
 */
interface DynamicUniform {
    fun write(buffer: ByteBuffer)
}
