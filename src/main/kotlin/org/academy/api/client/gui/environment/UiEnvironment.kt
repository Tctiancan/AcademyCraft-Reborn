package org.academy.api.client.gui.environment

import net.minecraft.resources.ResourceLocation
import org.academy.api.client.gui.texture.TextureSource
import org.academy.api.client.gui.unit.Density
import org.academy.api.client.render.texture.GpuTextureView
import java.io.InputStream
import java.nio.file.Path

interface UiEnvironment {
    val guiScaledWidth: Int

    val guiScaledHeight: Int

    val guiScale: Float

    val density: Density get() = Density(guiScale)

    val physicalWidth: Int

    val physicalHeight: Int

    val gameDirectory: Path

    fun runOnMainThread(task: Runnable)

    fun runOnRenderThread(task: Runnable)

    fun isOnMainThread(): Boolean

    fun isOnRenderThread(): Boolean

    fun frameDeltaTicks(): Float

    fun openResource(namespace: String, path: String): InputStream?

    fun loadTexture(identifier: ResourceLocation): GpuTextureView

    fun createDynamicTextureSource(identifier: ResourceLocation, bytes: ByteArray): TextureSource

    fun clipboard(): String = ""

    fun setClipboard(text: String) {
    }

    fun textInputFocusChanged(focused: Boolean) {
    }

    companion object {
        @Volatile
        private var delegate: UiEnvironment = MinecraftUiEnvironment()

        @JvmStatic
        fun get(): UiEnvironment = delegate

        @JvmStatic
        fun set(environment: UiEnvironment) {
            delegate = environment
        }
    }
}
