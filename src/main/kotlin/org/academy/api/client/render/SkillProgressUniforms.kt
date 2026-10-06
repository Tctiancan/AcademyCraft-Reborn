package org.academy.api.client.render

import java.nio.ByteBuffer

class SkillProgressUniforms(val progress: Float) : DynamicUniform {
    override fun write(buffer: ByteBuffer) {
        Std140Builder.intoBuffer(buffer).putFloat(progress)
    }

    companion object {
        val UBO_SIZE: Int = Std140SizeCalculator().putFloat().get()
    }
}
