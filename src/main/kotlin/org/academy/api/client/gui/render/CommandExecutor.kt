package org.academy.api.client.gui.render

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.BufferUploader
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.MeshData
import com.mojang.blaze3d.vertex.VertexFormat
import net.minecraft.client.renderer.GameRenderer
import net.minecraft.client.renderer.ShaderInstance
import org.academy.api.client.gui.environment.UiEnvironment
import org.academy.api.client.render.TextureBinding
import kotlin.math.ceil
import kotlin.math.floor

/**
 * 移植自 Minecraft 26.2 的 GPU 命令编码器, 适配 1.21.1 即时渲染.
 *
 * 26.2 版本使用 device/commandEncoder/renderPass + CPU 环形 GpuBuffer 把网格上载后
 * 一次性命令编码提交; 1.21.1 无对应 API, 改为直接对 [BatchProcessor] 产出的
 * [MeshData] 走 [BufferUploader.drawWithShader] 即时上载绘制.
 *
 * 迁移策略 (C-stun):
 * - 几何: 按网格顶点格式选择内置 shader, 用 RenderSystem 全局矩阵/管线即时绘制.
 * - 纹理: 按 [TextureBinding] 的 sampler 槽绑定 1.21.1 [ResourceLocation].
 * - Uniform: 1.21.1 无 26.2 式 GPU UBO 通道, 首版整体跳过 (占位).
 * - Scissor: 复用原物理像素换算, 落到 [RenderSystem.enableScissor].
 */
class CommandExecutor : AutoCloseable {

    /**
     * 提交一批网格到当前 1.21.1 渲染目标.
     *
     * @param batches 已按批次合并、顶点已生成好的网格 (来自 [BatchProcessor]).
     * @param guiScale 物理像素 / GUI 空间像素之比, 用于 scissor 换算.
     */
    fun execute(batches: List<PendingBatch>, guiScale: Float) {
        if (batches.isEmpty()) return

        for (batch in batches) {
            if (!applyScissor(batch.scissorArea, guiScale)) continue
            drawBatch(batch)
        }
        RenderSystem.disableScissor()
    }

    private fun drawBatch(batch: PendingBatch) {
        bindTextures(batch.textures)

        for (mesh in batch.meshDataList) {
            drawMesh(mesh)
        }
    }

    private fun bindTextures(textures: List<TextureBinding>) {
        for (texture in textures) {
            val location = texture.location ?: continue
            RenderSystem.setShaderTexture(texture.samplerSlot, location)
        }
    }

    private fun drawMesh(mesh: MeshData) {
        val shader = shaderFor(mesh.drawState().format()) ?: return
        RenderSystem.setShader { shader }
        BufferUploader.drawWithShader(mesh)
        // MeshData 为 CPU 侧一次性分配; 完成后立即释放内存.
        mesh.close()
    }

    /**
     * 依据网格顶点格式选择内置 shader. 1.21.1 下 [BufferUploader.drawWithShader] 使用
     * RenderSystem 当前绑定的 shader, 其 attribute 布局须与网格格式一致.
     */
    private fun shaderFor(format: VertexFormat): ShaderInstance? {
        return when (format) {
            DefaultVertexFormat.POSITION -> GameRenderer.getPositionShader()
            DefaultVertexFormat.POSITION_COLOR -> GameRenderer.getPositionColorShader()
            DefaultVertexFormat.POSITION_TEX -> GameRenderer.getPositionTexShader()
            DefaultVertexFormat.POSITION_TEX_COLOR -> GameRenderer.getPositionTexColorShader()
            else -> null
        }
    }

    /**
     * 把 GUI 空间 scissor 矩形换算为 1.21.1 物理像素(左下原点)并启用; 退化矩形返回 false
     * 以跳过整个批次. 坐标语义沿用 26.2 原实现.
     */
    private fun applyScissor(scissor: ScissorRect?, guiScale: Float): Boolean {
        if (scissor == null) return true

        val physicalWidth = UiEnvironment.get().physicalWidth
        val physicalHeight = UiEnvironment.get().physicalHeight
        val pos = scissor.position
        val eps = 1e-4f
        val left = floor(pos.x * guiScale + eps).toInt()
        val right = ceil((pos.x + scissor.width) * guiScale - eps).toInt()
        val glBottom = floor(physicalHeight - (pos.y + scissor.height) * guiScale + eps).toInt()
        val glTop = ceil(physicalHeight - pos.y * guiScale - eps).toInt()

        val x = left.coerceIn(0, physicalWidth)
        val y = glBottom.coerceIn(0, physicalHeight)
        val w = (right.coerceIn(0, physicalWidth) - x).coerceAtLeast(0)
        val h = (glTop.coerceIn(0, physicalHeight) - y).coerceAtLeast(0)
        if (w <= 0 || h <= 0) return false

        RenderSystem.enableScissor(x, y, w, h)
        return true
    }

    override fun close() {
        // 1.21.1 即时模式下无跨帧 GPU 资源需要释放 (由 mesh.close / RenderSystem 管理).
    }
}
