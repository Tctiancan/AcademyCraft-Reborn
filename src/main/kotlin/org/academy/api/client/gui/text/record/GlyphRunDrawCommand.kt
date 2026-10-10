package org.academy.api.client.gui.text.record

import com.mojang.blaze3d.vertex.PoseStack
import org.academy.api.client.gui.command.DrawCommand
import org.academy.api.client.gui.command.LocalBounds
import org.academy.api.client.gui.command.PosColorRectDrawCommand
import org.academy.api.client.gui.render.VertexWriter
import org.academy.api.client.render.RenderPipeline
import org.academy.api.client.render.TextureBinding
import org.academy.api.client.render.UniformPayload
import org.academy.api.client.render.texture.FilterMode
import org.academy.api.client.render.texture.GpuTextureView
import org.joml.Vector3f
import kotlin.math.max
import kotlin.math.min

abstract class GlyphRunDrawCommand protected constructor(
    pipeline: RenderPipeline,
    textureView: GpuTextureView,
    private val quads: List<GlyphQuad>,
    uniforms: List<UniformPayload<*>>
) : DrawCommand(
    pipeline,
    listOf(
        TextureBinding(
            "Sampler0",
            textureView,
            org.academy.api.client.render.texture.SamplerCache.INSTANCE.getClampToEdge(FilterMode.LINEAR)
        )
    ),
    uniforms
) {
    final override fun isGeometryFixed(): Boolean = true

    override fun allowsOverlapMerge(): Boolean = true

    override fun localBounds(): LocalBounds {
        if (quads.isEmpty()) return LocalBounds(0f, 0f, 0f, 0f)
        var left = Float.MAX_VALUE
        var top = Float.MAX_VALUE
        var right = -Float.MAX_VALUE
        var bottom = -Float.MAX_VALUE
        for ((x, y, width, height) in quads) {
            left = min(left, x)
            top = min(top, y)
            right = max(right, x + width)
            bottom = max(bottom, y + height)
        }
        val aa = PosColorRectDrawCommand.AA
        return LocalBounds(left - aa, top - aa, right + aa, bottom + aa)
    }

    /**
     * 1.21.1 即时模式: 文本管线现为单绑定 POSITION_TEX_COLOR (见 Render.textPipeline), 不再走 26.2 的
     * 两绑定 GPU 实例化. 这里直接为每个字形生成一个完整四边形(经 pose 变换的坐标 + UV + 预乘颜色),
     * 与 [org.academy.api.client.gui.command.PosTexColorRectDrawCommand] 的写入方式保持一致.
     */
    final override fun generateVertices(writer: VertexWriter, pose: PoseStack.Pose, alphaMul: Float) {
        val matrix = pose.pose()
        val dest = Vector3f()
        for ((x, y, width, height, u0, v0, u1, v1, red, green, blue, alpha, fadeLeft, fadeRight) in quads) {
            val r = (red * 255.0f).toInt()
            val g = (green * 255.0f).toInt()
            val b = (blue * 255.0f).toInt()
            val aLeft = (alpha * alphaMul * fadeLeft * 255.0f).toInt()
            val aRight = (alpha * alphaMul * fadeRight * 255.0f).toInt()

            // (x, y) — 左缘淡出
            writer.beginVertex()
            matrix.transformPosition(x, y, 0f, dest)
            writer.putVec3f(dest.x, dest.y, dest.z)
            writer.putVec2f(u0, v0)
            writer.putColor(r, g, b, aLeft)

            // (x, y + height) — 左缘淡出
            writer.beginVertex()
            matrix.transformPosition(x, y + height, 0f, dest)
            writer.putVec3f(dest.x, dest.y, dest.z)
            writer.putVec2f(u0, v1)
            writer.putColor(r, g, b, aLeft)

            // (x + width, y + height) — 右缘淡出
            writer.beginVertex()
            matrix.transformPosition(x + width, y + height, 0f, dest)
            writer.putVec3f(dest.x, dest.y, dest.z)
            writer.putVec2f(u1, v1)
            writer.putColor(r, g, b, aRight)

            // (x + width, y) — 右缘淡出
            writer.beginVertex()
            matrix.transformPosition(x + width, y, 0f, dest)
            writer.putVec3f(dest.x, dest.y, dest.z)
            writer.putVec2f(u1, v0)
            writer.putColor(r, g, b, aRight)
        }
    }

    final override fun instanceCount(pose: PoseStack.Pose, alphaMul: Float): Int = quads.size

    final override fun appendInstances(slot: Int, writer: VertexWriter, pose: PoseStack.Pose, alphaMul: Float) {
        if (slot != 1) return
        val matrix = pose.pose()
        val start = Vector3f()
        val end = Vector3f()
        for ((x, y, width, height, u0, v0, u1, v1, red, green, blue, alpha, fadeLeft, fadeRight) in quads) {
            matrix.transformPosition(x, y, 0f, start)
            matrix.transformPosition(x + width, y + height, 0f, end)

            writer.beginVertex()
            writer.putVec3f(start.x, start.y, start.z)
            writer.putVec2f(end.x - start.x, end.y - start.y)
            writer.putVec2f(u0, v0)
            writer.putVec2f(u1, v1)
            writer.putVec4f(red, green, blue, alpha * alphaMul)
            writer.putVec2f(fadeLeft, fadeRight)
        }
    }
}
