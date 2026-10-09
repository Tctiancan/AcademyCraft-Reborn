package org.academy.api.client.gui.text.atlas

import com.mojang.blaze3d.platform.NativeImage
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.resources.ResourceLocation
import org.academy.api.client.render.texture.GpuTexture
import org.academy.api.client.render.texture.GpuTextureView
import org.academy.api.client.thread.runOnRenderThread
import java.nio.ByteBuffer

/**
 * 文本图集页.
 *
 * [texture] 背书一个纹理资源路径. 为避免 GPU 纹理在本应在渲染线程执行的分配被延迟到异步
 * 帧队列, 也避免用裸 `GL11.glTexSubImage2D` 直接写未完整初始化的存储 (不同驱动尤其是 AMD
 * 对格式不匹配的 sub-image 写入会触发 GL_INVALID_OPERATION 乃至 native 崩溃), 这里改为:
 *
 * 1. 在渲染线程创建并注册 [DynamicTexture], 以 [NativeImage] 作为 CPU 侧图集像素缓冲;
 * 2. [upload] 把区域像素写入对应图集坐标 (等效于真正的 atlas 子区域上传);
 * 3. 通过 [DynamicTexture.upload] 走 Minecraft 自带的格式正确的上传路径.
 *
 * 这样保证纹理存储与像素格式完全一致, 跨驱动稳定.
 */
class AtlasPage(
    val size: Int,
    label: String,
    /** 每像素字节数: 4 = RGBA (MSDF / R8 灰度放大为 RGBA), 1 = 灰度 (位图 R8). */
    bytesPerPixel: Int = 4
) {
    private val bytesPerPixel = if (bytesPerPixel >= 1) bytesPerPixel else 1

    private val location = ResourceLocation.fromNamespaceAndPath("academy", "text/atlas_page_" + label)

    /** 垫层视图: 仅背书一个纹理资源路径, 不持有实际 GPU 资源. */
    val texture: GpuTexture = GpuTexture.of(location)

    /** 由 [texture] 派生的视图, 供既有调用方 `view.location()` / `view.isClosed` 使用. */
    val textureView: GpuTextureView = texture.getTextureView()

    private val allocator = SkylineAllocator(size, size)

    // 仅在渲染线程内访问: 首次 upload 时同步创建并注册, 之后复用.
    @Volatile
    private var dynamicTexture: DynamicTexture? = null

    private fun ensureTexture(): DynamicTexture {
        var dt = dynamicTexture
        if (dt == null) {
            RenderSystem.assertOnRenderThreadOrInit()
            // 在渲染线程同步完成 CPU 缓冲创建与 GPU 存储分配, 避免异步 recordRenderCall 造成的竞态.
            dt = DynamicTexture(size, size, true)
            Minecraft.getInstance().textureManager.register(location, dt)
            dynamicTexture = dt
        }
        return dt
    }

    fun reserve(width: Int, height: Int): AtlasRect? = allocator.allocate(width, height)

    /**
     * 将 [buffer] 中 rect(width x height) 的像素写入 CPU 侧 NativeImage 的对应区域,
     * 再走 [DynamicTexture.upload] 原生路径上传整张纹理. [runOnRenderThread] 为同步等待.
     *
     * [buffer] 按 [bytesPerPixel] 解释: 每像素 1 字节表示灰度 (位图 R8), 4 字节表示 RGBA (MSDF).
     * 灰度会放大为 RGBA (R=G=B=F=灰度值, A=255) 以匹配底层 RGBA8 存储.
     */
    fun upload(rect: AtlasRect, buffer: ByteBuffer) {
        val width = rect.width
        val height = rect.height
        val bytesPerPixel = this.bytesPerPixel
        val required = width * height * bytesPerPixel
        buffer.position(0)
        // 备份原始 position, 供调用方继续读取(不改变其内容).
        val readLength = if (buffer.remaining() >= required) required else buffer.remaining()
        val data = ByteArray(readLength)
        buffer.get(data)
        buffer.position(0)
        runOnRenderThread {
            val dt = ensureTexture()
            val backing = dt.pixels ?: return@runOnRenderThread
            var idx = 0
            for (y in 0 until height) {
                for (x in 0 until width) {
                    if (idx + bytesPerPixel > data.size) return@runOnRenderThread
                    val color: Int
                    if (bytesPerPixel >= 4) {
                        val r = data[idx].toInt() and 0xFF
                        val g = data[idx + 1].toInt() and 0xFF
                        val b = data[idx + 2].toInt() and 0xFF
                        val a = data[idx + 3].toInt() and 0xFF
                        // NativeImage 内部为 ABGR.
                        color = a shl 24 or (b shl 16) or (g shl 8) or r
                    } else {
                        // 位图字形按覆盖度存储: 覆盖度放入 A, RGB 恒白. position_tex_color 会以
                        // 纹理RGB(白) × 顶点色(文本颜色) 得字体温色, 并以 纹理A(覆盖度) × 顶点A 作
                        // 透明度 alpha, 从而经 SRC_ALPHA 混合出正确的单色抗锯齿字形.
                        val v = data[idx].toInt() and 0xFF
                        color = (v shl 24) or 0xFFFFFF
                    }
                    backing.setPixelRGBA(rect.x + x, rect.y + y, color)
                    idx += bytesPerPixel
                }
            }
            // Minecraft 原生上传路径: 绑定 + glTexImage2D, 格式正确且跨驱动稳定.
            dt.upload()
        }
    }

    /**
     * 返回当前 CPU 侧像素栅格 (渲染线程内访问). 若尚未经 [upload] 创建过纹理则返回 null.
     * 供调试导出 (PNG) 使用; 返回的 [NativeImage] 由本对象持有, 调用方不得释放.
     */
    fun pixelBuffer(): NativeImage? {
        RenderSystem.assertOnRenderThread()
        return dynamicTexture?.pixels
    }

    fun close() {
        runOnRenderThread {
            val dt = dynamicTexture
            dynamicTexture = null
            dt?.close()
        }
    }
}
