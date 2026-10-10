package org.academy.internal.client.gui.imgui

import com.mojang.blaze3d.pipeline.RenderTarget
import imgui.ImGui
import imgui.ImVec4
import imgui.extension.implot.ImPlot
import imgui.flag.ImGuiConfigFlags
import imgui.glfw.ImGuiImplGlfw
import imgui.type.ImInt
import org.academy.AcademyCraft
import org.academy.api.client.render.texture.GpuSampler
import org.academy.api.client.render.texture.GpuTextureView
import org.jetbrains.annotations.ApiStatus
import java.io.IOException
import java.lang.ref.Reference
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

private val LOGGER = AcademyCraft.getLogger()

/**
 * ImGui 后端.
 *
 * 26.2 中本类依赖 GPU 设备命令编码 (GpuBuffer / CommandEncoder / RenderPass / IndexType /
 * RenderSystem.getDevice) 将绘制提交到离屏 [RenderTarget]; 这些 API 在 1.21.1 已被移除.
 * 首版以 C-stun 保留 CPU 侧 ImGui 状态机 (init / 字体图集准备 / 帧生命周期 /
 * registerTexture/unregisterTexture 契约), 跳过实际 GPU 提交绘制.
 */
@ApiStatus.Internal
class ImGuiBackend(
    private val windowHandle: Long,
) {
    val imGuiImplGlfw = ImGuiImplGlfw()

    private var initialized = false

    private var fontAtlasPixels: ByteBuffer? = null
    private var fontAtlasWidth = 0
    private var fontAtlasHeight = 0

    private val reusableClipRect = ImVec4()

    private val textures = ConcurrentHashMap<Long, Pair<GpuTextureView, GpuSampler>>()
    private val nextTextureId = AtomicLong(2L)

    companion object {
        const val FONT_TEX_ID = 1L
    }

    fun registerTexture(view: GpuTextureView, sampler: GpuSampler): Long {
        val id = nextTextureId.getAndIncrement()
        textures[id] = Pair(view, sampler)
        return id
    }

    fun unregisterTexture(id: Long) {
        textures.remove(id)
    }

    fun init() {
        if (initialized) return
        ImGui.createContext()
        ImPlot.createContext()

        val io = ImGui.getIO()
        io.fontGlobalScale = 1f
        io.configFlags = ImGuiConfigFlags.DockingEnable or ImGuiConfigFlags.NavEnableKeyboard
        loadLocalizedFont()

        imGuiImplGlfw.init(windowHandle, true)
        initialized = true
    }

    private fun loadLocalizedFont() {
        val fonts = ImGui.getIO().fonts
        val fontResource = ImGuiBackend::class.java
            .getResourceAsStream("/assets/academy/fonts/wqy-microhei-modified.ttf")
        if (fontResource == null) {
            LOGGER.warn("[ImGui] Localized font resource is missing; using the built-in font")
            fonts.addFontDefault()
            cacheFontAtlas()
            return
        }

        val temporaryFont = try {
            Files.createTempFile("academy-imgui-font-", ".ttf")
        } catch (exception: IOException) {
            fontResource.close()
            LOGGER.error("[ImGui] Failed to create a temporary localized font; using the built-in font", exception)
            fonts.addFontDefault()
            cacheFontAtlas()
            return
        }
        try {
            try {
                fontResource.use {
                    Files.copy(it, temporaryFont, StandardCopyOption.REPLACE_EXISTING)
                }
            } catch (exception: IOException) {
                LOGGER.error("[ImGui] Failed to stage the localized font; using the built-in font", exception)
                fonts.addFontDefault()
                cacheFontAtlas()
                return
            }

            val glyphRanges = fonts.glyphRangesChineseSimplifiedCommon
            fonts.addFontFromFileTTF(temporaryFont.toString(), 14f, glyphRanges)
            cacheFontAtlas()
            Reference.reachabilityFence(glyphRanges)
        } finally {
            try {
                Files.deleteIfExists(temporaryFont)
            } catch (exception: IOException) {
                LOGGER.warn("[ImGui] Failed to delete temporary font {}", temporaryFont, exception)
            }
        }
    }

    private fun cacheFontAtlas() {
        val fonts = ImGui.getIO().fonts
        val width = ImInt()
        val height = ImInt()
        val pixels = fonts.getTexDataAsRGBA32(width, height)
        val atlasWidth = width.get()
        val atlasHeight = height.get()
        val expectedSize = Math.multiplyExact(Math.multiplyExact(atlasWidth, atlasHeight), 4)

        require(atlasWidth > 0 && atlasHeight > 0 && pixels.capacity() >= expectedSize) {
            "ImGui produced an invalid font atlas: ${atlasWidth}x$atlasHeight, ${pixels.capacity()} bytes"
        }

        fontAtlasPixels = pixels
        fontAtlasWidth = atlasWidth
        fontAtlasHeight = atlasHeight

        fonts.clearInputData()
        LOGGER.info(
            "[ImGui] Prepared font atlas {}x{}",
            atlasWidth,
            atlasHeight
        )
    }

    fun render(renderTarget: RenderTarget, renderCommand: () -> Unit) {
        if (!initialized) return

        newFrame()

        imGuiImplGlfw.newFrame()
        ImGui.newFrame()
        renderCommand()
        ImGui.render()

        // C-stun: 26.2 的 GPU 提交 (createCommandEncoder / createRenderPass / uploadDrawData /
        // renderDrawData) 依赖 GpuBuffer/CommandEncoder/RenderPass/IndexType/RenderSystem.getDevice,
        // 在 1.21.1 均被移除. 首版仅推进 ImGui 帧状态, 跳过实际绘制.
    }

    private fun newFrame() {
        // C-stun: 26.2 下此处在首次渲染时创建 ProjMtx UBO 与字体 GPU 纹理;
        // 1.21.1 无 GPU 设备命令编码, 首版置空.
    }

    fun clearEventsQueue() {
        ImGui.getIO().clearEventsQueue()
    }

    fun wantCaptureMouse(): Boolean = ImGui.getIO().wantCaptureMouse

    fun wantCaptureKeyboard(): Boolean = ImGui.getIO().wantCaptureKeyboard

    fun dispose() {
        if (!initialized) return

        fontAtlasPixels = null
        fontAtlasWidth = 0
        fontAtlasHeight = 0
        textures.clear()

        imGuiImplGlfw.shutdown()
        ImPlot.destroyContext()
        ImGui.destroyContext()
        initialized = false
    }
}
