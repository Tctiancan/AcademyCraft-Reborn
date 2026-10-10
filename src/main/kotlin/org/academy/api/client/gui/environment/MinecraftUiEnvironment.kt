package org.academy.api.client.gui.environment

import com.mojang.blaze3d.platform.NativeImage
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.resources.ResourceLocation
import org.academy.api.client.gui.texture.IdentifierTextureSource
import org.academy.api.client.gui.texture.TextureSource
import org.academy.api.client.render.texture.GpuTextureView
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.nio.file.Path

class MinecraftUiEnvironment : UiEnvironment {
    private val mc get() = Minecraft.getInstance()

    override val guiScaledWidth: Int get() = mc.window.guiScaledWidth
    override val guiScaledHeight: Int get() = mc.window.guiScaledHeight
    override val guiScale: Float get() = mc.window.guiScale.toFloat()
    override val physicalWidth: Int get() = mc.window.width
    override val physicalHeight: Int get() = mc.window.height
    override val gameDirectory: Path get() = mc.gameDirectory.toPath()
    override fun runOnMainThread(task: Runnable) = mc.execute(task)

    override fun runOnRenderThread(task: Runnable) = mc.execute(task)

    override fun isOnMainThread(): Boolean = mc.isSameThread
    override fun isOnRenderThread(): Boolean = RenderSystem.isOnRenderThread()

    override fun frameDeltaTicks(): Float = mc.timer.gameTimeDeltaTicks
    override fun openResource(namespace: String, path: String): InputStream? =
        mc.resourceManager.getResource(ResourceLocation.fromNamespaceAndPath(namespace, path)).map { it.open() }.orElse(null)

    override fun loadTexture(identifier: ResourceLocation): GpuTextureView =
        GpuTextureView.of(identifier)

    override fun createDynamicTextureSource(identifier: ResourceLocation, bytes: ByteArray): TextureSource {
        val image = NativeImage.read(ByteArrayInputStream(bytes))
        val texture = DynamicTexture(image)
        mc.textureManager.register(identifier, texture)
        return IdentifierTextureSource(identifier, this)
    }

    override fun clipboard(): String = mc.keyboardHandler.clipboard
    override fun setClipboard(text: String) {
        mc.keyboardHandler.clipboard = text
    }

    override fun textInputFocusChanged(focused: Boolean) {
        // 1.21.1 无 TextInputManager (26.2 新增的IME输入焦点抽象)。
        // 字符输入由各 EditBox 组件及窗口内部处理，此处保持方法存在但不做额外处理。
    }
}
