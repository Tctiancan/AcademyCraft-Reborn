package org.academy.desktop.platform

import com.mojang.blaze3d.pipeline.RenderTarget
import com.mojang.blaze3d.pipeline.TextureTarget
import org.academy.blaze3d.GpuFormat
import org.academy.blaze3d.textures.GpuTexture
import org.academy.blaze3d.textures.GpuTextureView

/**
 * 编辑器视口 / 后处理对 26.2 {@code RenderTarget / TextureTarget} API 的 1.21.1 适配垫层.
 *
 * <p>1.21.1 的 {@link RenderTarget} 没有 26.2 的
 * {@code getColorTextureView()/getDepthTextureView()/destroyBuffers()} 以及字符串+格式的
 * {@code TextureTarget} 构造; 这些方法以 Kotlin 扩展/工厂保持既有调用形态成立 (C-stun):
 * 返回的纹理视图背书占位纹理, 渲染为空操作, 不产生真实 GPU 资源.
 */

/** 返回颜色附件视图 (C-stun: 占位视图, 无真实 GPU 资源). */
fun RenderTarget.getColorTextureView(): GpuTextureView =
    GpuTextureView(GpuTexture.of(null))

/** 返回深度附件视图; 1.21.1 下深度附件不参与编辑器视口合成, 返回 null. */
fun RenderTarget.getDepthTextureView(): GpuTextureView? = null

/**
 * 26.2 风格 {@code TextureTarget(label, width, height, clearOnRender, format)} 的工厂适配:
 * 映射到 1.21.1 的 {@code TextureTarget(width, height, samples, clearOnRender)}.
 */
fun textureTarget(
    @Suppress("UNUSED_PARAMETER") label: String,
    width: Int,
    height: Int,
    clearOnRender: Boolean,
    @Suppress("UNUSED_PARAMETER") format: GpuFormat,
): TextureTarget =
    TextureTarget(width, height, false, clearOnRender)
