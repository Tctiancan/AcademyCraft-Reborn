package org.academy.desktop.platform

import com.mojang.blaze3d.textures.GpuTexture
import com.mojang.blaze3d.textures.GpuTextureView
import net.minecraft.resources.ResourceLocation
import java.io.InputStream
import java.util.concurrent.ConcurrentHashMap

/**
 * 编辑器桌面纹理加载/上传 (C-stun).
 *
 * 26.2 版本通过 {@code RenderSystem.getDevice()} 创建 GPU 纹理并用 CommandEncoder 写像素,
 * 且依赖 {@link com.mojang.blaze3d.platform.NativeImage#getPixelsABGR()}; 这些 API 在 1.21.1
 * 已不存在. 首版退化为占位实现: 只登记标识, 返回背书 {@link ResourceLocation} 的占位纹理视图,
 * 不产生任何真实 GPU 资源. 保留注册/加载/释放契约以维持调用方源码兼容.
 */
object DesktopTextures {
    private val dynamicIds = ConcurrentHashMap.newKeySet<ResourceLocation>()

    private val uploadedIds = ConcurrentHashMap.newKeySet<ResourceLocation>()

    private val missing: GpuTextureView by lazy { GpuTextureView(GpuTexture.of(null)) }

    fun register(identifier: ResourceLocation, bytes: ByteArray) {
        // C-stun: 1.21.1 不进行 CPU→GPU 纹理上传, 仅登记标识.
        dynamicIds.add(identifier)
    }

    fun load(identifier: ResourceLocation, input: InputStream?): GpuTextureView {
        if (dynamicIds.contains(identifier) || uploadedIds.contains(identifier)) {
            return GpuTextureView(GpuTexture.of(identifier))
        }
        if (input == null) return missing
        uploadedIds.add(identifier)
        return GpuTextureView(GpuTexture.of(identifier))
    }

    fun close() {
        dynamicIds.clear()
        uploadedIds.clear()
    }
}
