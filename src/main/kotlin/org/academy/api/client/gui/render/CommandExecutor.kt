package org.academy.api.client.gui.render

import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.BufferUploader
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.MeshData
import com.mojang.blaze3d.vertex.VertexFormat
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GameRenderer
import net.minecraft.client.renderer.ShaderInstance
import net.minecraft.resources.ResourceLocation
import org.academy.api.client.gui.environment.UiEnvironment
import org.academy.api.client.render.RenderPipeline
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
     * 按 progress 裁切的专业度环专用着色器. 1.21.1 [BufferUploader.drawWithShader] 默认
     * 按网格顶点格式选内置 shader, 无法采样第二纹理/携带进度 uniform; 故 skill_progress 管线
     * 单独实例化 [ShaderInstance] (双采样 Sampler0/Sampler1 + SkillProgress), 复刻 26.2
     * 径向遮罩按角度裁切的效果. 渲染线程懒加载一次后复用.
     */
    private var skillProgressShader: ShaderInstance? = null

    /**
     * 圆角底盘(BgIcons/WirelessPanel 的半透明深色背景)专用的 SDF 边缘柔化着色器.
     * 26.2 中该管线把 SdfUniforms (Size/Margins/FillColor) 经 UBO 上传; 1.21.1 的
     * [ShaderInstance] 无法访问 GPU uniform 块, 故 fsh 改为逐个 uniform, 渲染线程
     * 懒加载一次后复用, 并在每次绘制前从批次提取 SDFData 重新上传.
     */
    private var sdfSharpMarginShader: ShaderInstance? = null

    /** skill_progress 管线名, 用于把该管线的批次路由到自定义 shader. */
    private companion object {
        const val SKILL_PROGRESS_PIPELINE = "skill_progress"
        const val SDF_SHARP_MARGIN_PIPELINE = "sdf_sharp_margin"
    }

    /**
     * 提交一批网格到当前 1.21.1 渲染目标.
     *
     * @param batches 已按批次合并、顶点已生成好的网格 (来自 [BatchProcessor]).
     * @param guiScale 物理像素 / GUI 空间像素之比, 用于 scissor 换算.
     */
    fun execute(batches: List<PendingBatch>, guiScale: Float) {
        if (batches.isEmpty()) return

        for (batch in batches) {
            val scissor = batch.scissorArea
            if (scissor == null) {
                // 无裁剪批次必须显式清掉上一个批次残留的 scissor, 否则会沿用旧区域把内容裁掉
                // (后果: 文本整段不可见、配方等只显示残留 scissor 覆盖到的左半边).
                RenderSystem.disableScissor()
            } else {
                if (!applyScissor(scissor, guiScale)) continue
            }
            drawBatch(batch)
        }
        // 本 HUD 为即时绘制, 按批次开启的 blend 及其混合函数需在返回前复位, 否则会残留到
        // 随后的渲染阶段. 尤其暂停菜单紧跟 HUD 做 renderBlurredBackground -> processBlurEffect
        // 的全屏模糊 PostChain, 而 PostPass.process 并不强制设置混合状态, 会继承此处残留的
        // blend, 导致模糊/深色背景被污染而出现大量黑块. 背包/方块界面用的是
        // renderTransparentBackground (纯渐变、不触发模糊后处理), 故不受影响, 与现象一致.
        RenderSystem.disableBlend()
        RenderSystem.defaultBlendFunc()
        RenderSystem.disableScissor()
    }

    private fun drawBatch(batch: PendingBatch) {
        configureBlend(batch.pipeline)
        bindTextures(batch.textures)

        if (batch.pipeline.name == SKILL_PROGRESS_PIPELINE) {
            drawSkillProgress(batch)
            return
        }

        if (batch.pipeline.name == SDF_SHARP_MARGIN_PIPELINE) {
            drawSdfSharpMargin(batch)
            return
        }

        for (mesh in batch.meshDataList) {
            drawMesh(mesh)
        }
    }

    /**
     * 为每个批次设置确定性的混合状态, 避免继承上一个批次 / 原生 GUI 遗留的混合模式.
     * 本 UI 顶点均以直线(非预乘)RGBA 写出, 故常规管线用 [RenderSystem.defaultBlendFunc]
     * (SRC_ALPHA, ONE_MINUS_SRC_ALPHA); 否则半透明白色矩形 (r=g=b=1, a<1) 若在上一个
     * 预乘混合残留下绘制会得到 out = rgb + dst*(1-a) ≈ 白, 表现为整块泛白、文本被淹没.
     * 名含 "premultiplied" 的管线保持其预乘混合语义不变, 避免回归.
     */
    private fun configureBlend(pipeline: RenderPipeline) {
        RenderSystem.enableBlend()
        if (pipeline.name.contains("premultiplied")) {
            RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
            )
        } else {
            RenderSystem.defaultBlendFunc()
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
     * 使用自定义 skill_progress 着色器绘制专业度环: 同时采样 Sampler0 (轮廓) 与 Sampler1
     * (径向遮罩), 依据批次上的 SkillProgress uniform 在片段着色器内按角度裁切, 复刻 26.2 效果.
     * 顶点/UV(0..1 全域) 由 [PosColorRectDrawCommand] 基类生成单个全矩形四边形.
     */
    private fun drawSkillProgress(batch: PendingBatch) {
        val shader = skillProgressShader()
            ?: run {
                // shader 加载失败(编译/链接错误)时跳过本批次, 但仍需释放其网格资源.
                for (mesh in batch.meshDataList) mesh.close()
                return
            }
        RenderSystem.setShader { shader }

        var progress = -1f
        for (binding in batch.uniforms) {
            if (binding.name() == "SkillProgress") {
                progress = binding.data().getFloat(0).coerceIn(0f, 1f)
                break
            }
        }
        val uniform = shader.safeGetUniform("SkillProgress")
        uniform.set(progress)

        for (mesh in batch.meshDataList) {
            BufferUploader.drawWithShader(mesh)
            // MeshData 为 CPU 侧一次性分配; 完成后立即释放内存.
            mesh.close()
        }
    }

    /**
     * 使用自定义 sdf_sharp_margin 着色器绘制圆角底盘 (BlendQuadWidget 的黑色半透明背景).
     * 1.21.1 下 [PositionTexRectDrawCommand] 仅写位置+UV, 无内置颜色; 若走内置 position_tex
     * 着色器会去采样当前绑定的 Sampler0 (往往是刚绘制过的图标纹理), 把图标拉伸成大块"幽灵"
     * 半透明叠在背景上. 这里为批次绑定 SDF 着色器, 并从 [BlendQuadWidget.SDFData] 提取
     * Size/Margins/FillColor 后经普通 uniform 上传, 复刻 26.2 的圆角羽化填充.
     */
    private fun drawSdfSharpMargin(batch: PendingBatch) {
        val shader = sdfSharpMarginShader()
            ?: run {
                for (mesh in batch.meshDataList) mesh.close()
                return
            }
        RenderSystem.setShader { shader }

        for (binding in batch.uniforms) {
            if (binding.name() == "SdfUniforms") {
                uploadSdfUniforms(shader, binding.data())
                break
            }
        }

        for (mesh in batch.meshDataList) {
            BufferUploader.drawWithShader(mesh)
            mesh.close()
        }
    }

    /**
     * 从 [BatchProcessor] 序列化出的 std140 [ByteBuffer] 提取 SDFData (Size/Margins/FillColor),
     * 逐 uniform 上传到 sdf_sharp_margin 着色器. 偏移严格对应 [BlendQuadWidget.SDFData] 的
     * [org.academy.api.client.render.Std140Builder] 布局: vec2(align8, 8B) + pad8 + vec4*2(align16).
     */
    private fun uploadSdfUniforms(shader: ShaderInstance, buf: java.nio.ByteBuffer) {
        val sizeX = buf.getFloat(0); val sizeY = buf.getFloat(4)
        val mx = buf.getFloat(16); val my = buf.getFloat(20)
        val mz = buf.getFloat(24); val mw = buf.getFloat(28)
        val fx = buf.getFloat(32); val fy = buf.getFloat(36)
        val fz = buf.getFloat(40); val fw = buf.getFloat(44)

        shader.safeGetUniform("Size")?.set(sizeX, sizeY)
        shader.safeGetUniform("Margins")?.set(mx, my, mz, mw)
        shader.safeGetUniform("FillColor")?.set(fx, fy, fz, fw)
    }

    /**
     * 渲染线程懒加载 sdf_sharp_margin 着色器. 网格顶点格式为位置+UV.
     * 加载失败(编译/链接错误)时返回 null 并跳过该批次.
     */
    private fun sdfSharpMarginShader(): ShaderInstance? {
        if (sdfSharpMarginShader != null) return sdfSharpMarginShader
        sdfSharpMarginShader = try {
            ShaderInstance(
                Minecraft.getInstance().getResourceManager(),
                ResourceLocation.fromNamespaceAndPath("academy", "sdf_sharp_margin"),
                DefaultVertexFormat.POSITION_TEX
            )
        } catch (e: Exception) {
            sdfSharpMarginShader = null
            null
        }
        return sdfSharpMarginShader
    }

    /**
     * 渲染线程懒加载 skill_progress 着色器. 加载发生在绘制批次时 (渲染线程), 其后每次
     * [BufferUploader.drawWithShader] 都会经 setDefaultUniforms 重新绑定 Sampler0/Sampler1.
     */
    private fun skillProgressShader(): ShaderInstance? {
        if (skillProgressShader != null) return skillProgressShader
        skillProgressShader = try {
            ShaderInstance(
                Minecraft.getInstance().getResourceManager(),
                ResourceLocation.fromNamespaceAndPath("academy", "skill_progress"),
                DefaultVertexFormat.POSITION_TEX_COLOR
            )
        } catch (e: Exception) {
            skillProgressShader = null
            null
        }
        return skillProgressShader
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
        try {
            skillProgressShader?.close()
        } catch (_: Exception) {
            // 忽略关闭期异常, 避免污染渲染线程.
        }
        try {
            sdfSharpMarginShader?.close()
        } catch (_: Exception) {
            // 忽略关闭期异常, 避免污染渲染线程.
        }
        skillProgressShader = null
        sdfSharpMarginShader = null
    }
}
