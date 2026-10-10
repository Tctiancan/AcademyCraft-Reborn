package org.academy.api.client.gui.render

import com.mojang.blaze3d.pipeline.RenderTarget
import org.academy.api.client.render.post.BackdropBlurEngine
import org.academy.api.client.render.texture.GpuTextureView
import org.joml.Vector4f

/**
 * 首版 C-stun: 1.21.1 暂不迁移 26.2 的模糊/compositor 双缓冲后处理
 * (`RenderTarget.getColorTextureView` / `Render.runBlitPass` / `Buffers.fsQuadVBNDC` 等
 * 26.2 专属 API 在 1.21.1 均已移除或重构). 调用方 [UiContext] 已改为把命令直接画到 target,
 * 因此本对象保留公开方法签名但为空实现, 不参与批次A主链路.
 */
object UiCompositor {
    internal val NEUTRAL_TINT = Vector4f(1f, 1f, 1f, 1f)

    fun composite(
        target: RenderTarget,
        above: GpuTextureView,
        regions: List<BlurRegion>,
        blur: BackdropBlurEngine,
    ) {
        // C-stun: 组合后处理跳过.
    }

    fun compositeLayer(
        target: RenderTarget,
        source: GpuTextureView,
        regions: List<BlurRegion>,
        blur: BackdropBlurEngine,
    ) {
        // C-stun: 组合后处理跳过.
    }

    private fun applyBlurRegions(
        target: RenderTarget,
        regions: List<BlurRegion>,
        blur: BackdropBlurEngine,
    ): GpuTextureView? = null

    fun blitSource(target: GpuTextureView, source: GpuTextureView) {
        // C-stun: 屏幕 blit 后处理跳过.
    }
}
