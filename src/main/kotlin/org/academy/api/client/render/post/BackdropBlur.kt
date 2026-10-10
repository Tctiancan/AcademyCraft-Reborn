package org.academy.api.client.render.post

import org.academy.api.client.render.texture.GpuTextureView
import org.joml.Vector4f
import kotlin.math.floor
import kotlin.math.log2
import kotlin.math.max

object BackdropBlur {
    const val SIGMA_SCALE = 0.5f

    const val MAX_LAYERS = 8

    fun layerRadius(level: Int, sigma: Float): Float = sigma * ((1 shl level) - 1)

    fun radiusToLevel(radius: Float, sigma: Float): Float {
        if (radius <= 0f) return 0f
        if (sigma <= 0f) return 0f
        return log2(1f + radius / sigma)
    }

    fun layersFor(radius: Float, sigma: Float): Int {
        val k = radiusToLevel(radius, sigma)
        if (k <= 0f) return 1
        val lo = floor(k).toInt()
        val need = lo + 1 + if (k > lo) 1 else 0
        return need.coerceAtMost(MAX_LAYERS).coerceAtLeast(1)
    }

    fun clampLevel(level: Float, layerCount: Int): Float {
        val max = layerCount - 1
        if (level <= 0f) return 0f
        return level.coerceAtMost(max.toFloat())
    }

    fun levelSpan(radius: Float, sigma: Float, layerCount: Int): LevelSpan {
        val k = clampLevel(radiusToLevel(radius, sigma), layerCount)
        val lo = floor(k).toInt()
        val frac = k - floor(k)
        val hi = if (frac <= 0f) lo else (lo + 1).coerceAtMost(layerCount - 1)
        return LevelSpan(lo, hi, frac)
    }

    fun pyramidSize(width: Int, height: Int, levelCount: Int): List<Pair<Int, Int>> {
        val sizes = ArrayList<Pair<Int, Int>>(levelCount)
        var w = width
        var h = height
        for (i in 0 until levelCount) {
            sizes.add(w to h)
            w = max(1, w / 2)
            h = max(1, h / 2)
        }
        return sizes
    }

    /**
     * C-stun: 1.21.1 暂不迁移 26.2 的 GPU 高斯模糊后处理.
     * 原实现依赖 26.2 专属 API (Render.Buffers.getResourcePool / Render.runBlitPass /
     * Render.Buffers.fsQuadVBNDC / Render.BlurUniforms 等) 在 1.21.1 均已被移除或重构,
     * 首版以空实现呈现, 保留签名以维持 BlurEffect 等调用方源码兼容.
     */
    @JvmStatic
    fun applyGaussian(
        sampler: GpuTextureView,
        output: GpuTextureView,
        depth: GpuTextureView?,
        width: Int,
        height: Int,
        radius: Float,
    ) {
        // C-stun: 高斯模糊后处理跳过.
    }

    data class LevelSpan(val lo: Int, val hi: Int, val frac: Float) {
        val isIdentity: Boolean
            get() = lo == 0 && hi == 0
    }
}

/**
 * C-stun 引擎桩: 保留 [UiContext]/[UiCompositor] 所需的公开 API.
 * 原 26.2 实现持有离屏模糊金字塔 (TextureTarget) 与 UBO (GpuBuffer),
 * 依赖 26.2 GPU 设备命令编码; 1.21.1 首版以空实现跳过.
 */
class BackdropBlurEngine {
    fun close() {
        // C-stun: 离屏模糊金字塔资源已略去.
    }

    fun capture(source: GpuTextureView, maxRadius: Float) {
        // C-stun: 离屏捕获跳过.
    }

    fun fillRegion(
        target: GpuTextureView,
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        radius: Float,
        tint: Vector4f,
    ) {
        // C-stun: 模糊区域填充跳过.
    }
}
