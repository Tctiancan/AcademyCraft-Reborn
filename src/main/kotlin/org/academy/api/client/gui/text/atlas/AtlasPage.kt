package org.academy.api.client.gui.text.atlas

import net.minecraft.resources.ResourceLocation
import org.academy.api.client.render.texture.GpuTexture
import org.academy.api.client.render.texture.GpuTextureView
import java.nio.ByteBuffer

/**
 * 文本图集页.
 *
 * 布局分配保持原状; 26.2 中通过 [com.mojang.blaze3d.systems.RenderSystem.getDevice] 创建的 GPU 纹理
 * 在 1.21.1 已被移除. 此处改用 {@link GpuTexture} / {@link GpuTextureView} 垫层,
 * {@code upload}/{@code close} 以 C-stun 空实现呈现 (首版不真正上传像素),
 * 待后续迁移使用 1.21.1 的 {@code DynamicTexture} 时再落地上传逻辑.
 */
class AtlasPage(
    val size: Int,
    label: String
) {
    private val location = ResourceLocation.fromNamespaceAndPath("academy", "text/atlas_page_" + label)

    /** C-stun 垫层: 仅背书一个纹理资源路径, 不持有实际 GPU 资源. */
    val texture: GpuTexture = GpuTexture.of(location)

    /** C-stun 垫层: 由 [texture] 派生的视图, 供既有调用方 `view.location()` / `view.isClosed` 使用. */
    val textureView: GpuTextureView = texture.getTextureView()

    private val allocator = SkylineAllocator(size, size)

    fun reserve(width: Int, height: Int): AtlasRect? = allocator.allocate(width, height)

    /**
     * C-stun: 1.21.1 首版不真正上传像素到 GPU.
     * 原实现调用 26.2 的 command encoder `writeToTexture`; 该 API 在 1.21.1 不存在.
     */
    fun upload(rect: AtlasRect, buffer: ByteBuffer) {
        // C-stun: 文本图集上传跳过; 像素暂不提交到任意 GPU 纹理.
    }

    fun close() {
        // C-stun: 垫层不持有 GPU 资源, 无需释放.
    }
}
