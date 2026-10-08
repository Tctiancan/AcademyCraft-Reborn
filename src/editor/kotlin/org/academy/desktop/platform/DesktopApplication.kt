package org.academy.desktop.platform

import com.mojang.blaze3d.platform.DisplayData
import com.mojang.blaze3d.platform.Monitor
import com.mojang.blaze3d.platform.MonitorCreator
import com.mojang.blaze3d.platform.ScreenManager
import com.mojang.blaze3d.platform.Window
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.Util
import org.academy.api.client.gui.environment.UiEnvironment
import org.academy.api.client.gui.text.font.FontRepository
import org.lwjgl.glfw.GLFW
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.OptionalInt

object DesktopApplication {
    private val LOGGER: Logger = LoggerFactory.getLogger(DesktopApplication::class.java)

    private const val NANOS_PER_TICK = 50_000_000L

    fun run(app: EditorApp, environment: DesktopEnvironment) {
        UiEnvironment.set(environment)

        RenderSystem.initRenderThread()

        ClasspathShaderSource.sourceDir = environment.workingDir.resolve("src").resolve("main").resolve("resources")

        GLFW.glfwDefaultWindowHints()
        GLFW.glfwWindowHint(131088, GLFW.GLFW_TRUE)

        val host = DesktopUiHost(app, environment)
        val window = Window(
            host,
            ScreenManager { handle -> Monitor(handle) },
            DisplayData(
                environment.physicalWidth,
                environment.physicalHeight,
                OptionalInt.empty(),
                OptionalInt.empty(),
                false
            ),
            null,
            app.title,
        )

        host.bind(window)

        // C-stun: 1.21.1 为即时态渲染, 26.2 的 GpuDevice.createDevice / GpuSurface /
        // Render.Buffers / 管线预编译等启动链路均已移除, 首版跳过 GPU 初始化, 仅驱动帧回调.
        initFonts()

        GLFW.glfwShowWindow(window.getWindow())

        var lastNanos = 0L
        var lastTitle: String? = null
        while (!window.shouldClose() && !app.quitRequested()) {
            val now = System.nanoTime()
            val partialTick = ((now - lastNanos).toFloat() / NANOS_PER_TICK).coerceIn(0f, 1f)
            lastNanos = now

            if (app.title != lastTitle) {
                lastTitle = app.title
                window.setTitle(app.title)
            }

            GLFW.glfwPollEvents()

            host.frame(partialTick)
            window.updateDisplay()
        }

        host.close()
        app.onDispose()
        window.close()
    }

    private fun configureSurface(surface: Any, window: Window) {
        // C-stun: 1.21.1 无 GpuSurface 呈现配置.
    }

    private fun precompilePipelines(device: Any) {
        // C-stun: 1.21.1 无编译期管线预编译.
    }

    private fun initFonts() {
        try {
            FontRepository.loadFont(FontRepository.DEFAULT_FONT_ID)
            val cjkFont = FontRepository.DEFAULT_FONT_ID.withPath("fonts/wqy-microhei-modified.ttf")
            FontRepository.loadFont(cjkFont)
            FontRepository.setFontSearchOrder(listOf(FontRepository.DEFAULT_FONT_ID, cjkFont))
        } catch (e: Exception) {
            LOGGER.warn("Failed to initialize fonts", e)
        }
    }
}
