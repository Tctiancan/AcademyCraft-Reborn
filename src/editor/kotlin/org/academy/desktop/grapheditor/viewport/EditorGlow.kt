package org.academy.desktop.grapheditor.viewport

import org.com.mojang.blaze3d.textures.GpuTextureView
import org.academy.api.client.render.vfxgraph.arc.ArcBuffer
import org.academy.api.client.render.vfxgraph.render.GraphCamera
import org.academy.api.client.render.vfxgraph.render.RenderSpec
import org.academy.api.client.render.vfxgraph.render.VfxGraphRenderer
import org.academy.api.client.render.vfxgraph.render.WorldTransform
import org.academy.api.client.render.vfxgraph.sim.ParticleBuffer

/**
 * 编辑器视口 glow 复用（M21）：把 glow 混合效果的发光主体（GLOW 输出规格）渲进离屏输入，
 * 复用游戏同款 GAUSSIAN_BLUR 降采样模糊，再以 GLOW_BLEND 叠加回视口。
 *
 * C-stun：1.21.1 首版不移植编辑器视口 GPU 辉光合成 —— 26.2 独有的 CommandEncoder /
 * Render.runBlitPass / BackdropBlur 高斯链路及相关 UBO 均已移除或重构。保留类结构、
 * 构造器与渲染入口，实现为空占位，维持调用方源码兼容。
 */
class EditorGlow(
    private val renderer: VfxGraphRenderer,
) {
    /** 释放全部 GPU 资源（视口/预览销毁时调用，防泄漏）。 */
    fun destroy() {
        // C-stun: 1.21.1 未分配离屏辉光资源。
    }

    fun render(
        viewport: GpuTextureView,
        depth: GpuTextureView?,
        buffer: ParticleBuffer,
        arcBuffer: ArcBuffer?,
        camera: GraphCamera,
        specs: List<RenderSpec>,
        viewportWidth: Int,
        viewportHeight: Int,
    ) {
        if (buffer.count() == 0 && (arcBuffer == null || arcBuffer.count() == 0)) return
        // C-stun: 1.21.1 不执行辉光后处理（bloom 链路已省略），仅保留调用形态。
        if (arcBuffer != null) renderer.setArcBuffer(arcBuffer)
        renderer.render(buffer, camera, false, specs, WorldTransform.identity(), true)
    }
}
