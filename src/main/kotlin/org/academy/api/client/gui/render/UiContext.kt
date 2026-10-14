package org.academy.api.client.gui.render

import com.mojang.blaze3d.pipeline.RenderTarget
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.ByteBufferBuilder
import net.minecraft.client.Minecraft
import org.academy.api.client.gui.command.ItemStackDrawCommand
import org.academy.api.client.gui.command.SubmittedCommand
import org.academy.api.client.gui.environment.UiEnvironment
import org.academy.api.client.gui.layout.MeasureSpec
import org.academy.api.client.gui.render.BatchProcessor.UboUploader
import org.academy.api.client.gui.widget.WidgetContainer
import org.academy.api.client.render.DynamicUniform
import org.academy.api.client.render.UniformPayload
import org.academy.api.client.thread.MainThread
import org.academy.api.client.thread.RenderThread
import org.academy.api.client.thread.runOnRenderThread
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

open class UiContext {
    private val commandList = AtomicReference<MutableList<SubmittedCommand>?>()

    @Volatile
    private var lastBlurRegions: List<BlurRegion> = emptyList()

    private var performedRoot: WidgetContainer? = null

    private val closed = AtomicBoolean(false)
    private val closing = AtomicBoolean(false)

    private val commandExecutor = CommandExecutor()

    init {
        runOnRenderThread { initOnRenderThread() }
    }

    @MainThread
    fun perform(rootWidget: WidgetContainer, mouseX: Double, mouseY: Double, partialTick: Float) {
        val env = UiEnvironment.get()
        perform(rootWidget, mouseX, mouseY, partialTick, env.guiScaledWidth.toFloat(), env.guiScaledHeight.toFloat())
    }

    @MainThread
    fun perform(
        rootWidget: WidgetContainer,
        mouseX: Double,
        mouseY: Double,
        partialTick: Float,
        width: Float,
        height: Float
    ) {
        if (closed.get() || closing.get()) return

        if (performedRoot !== rootWidget) {
            commandList.set(null)
            lastBlurRegions = emptyList()
            performedRoot = rootWidget
        }

        val widthSpec = MeasureSpec(MeasureSpec.Mode.EXACTLY, width)
        val heightSpec = MeasureSpec(MeasureSpec.Mode.EXACTLY, height)

        if (rootWidget.isLayoutDirty) {
            rootWidget.measure(widthSpec, heightSpec)
            rootWidget.layout(0f, 0f, width, height)
        }

        val context = Canvas()
        generateCommands(context, rootWidget, mouseX, mouseY, partialTick)
        commandList.set(context.commands)
        lastBlurRegions = context.blurRegions
    }

    fun blurRegions(): List<BlurRegion> = lastBlurRegions

    @RenderThread
    fun splitSegments(
        blurRegions: List<BlurRegion>
    ): Pair<List<SubmittedCommand>, List<Pair<List<SubmittedCommand>, List<BlurRegion>>>>? {
        val env = UiEnvironment.get()
        val commands = commandList.getAndSet(null) ?: return null
        if (commands.isEmpty()) return null
        val preparedCommands = prepareItemCommands(commands, env.guiScale)
        val segments = splitCommandsForBlurByIndex(preparedCommands, blurRegions)
        return preparedCommands to segments
    }

    @RenderThread
    fun drawCommands(
        target: RenderTarget,
        commands: List<SubmittedCommand>,
        clear: Boolean,
        guiScaledW: Float,
        guiScaledH: Float
    ) {
        val env = UiEnvironment.get()
        val effectiveScale = env.guiScale
        drawPrepared(target, commands.toMutableList(), clear, guiScaledW, guiScaledH, effectiveScale)
    }

    @RenderThread
    fun upload(target: RenderTarget, clear: Boolean) {
        val env = UiEnvironment.get()
        upload(target, clear, env.physicalWidth / env.guiScale, env.physicalHeight / env.guiScale)
    }

    @RenderThread
    fun upload(target: RenderTarget, clear: Boolean, guiScaledW: Float, guiScaledH: Float) {
        uploadSplit(target, clear, guiScaledW, guiScaledH, aboveTarget = null, blurRegions = emptyList())
    }

    @RenderThread
    fun uploadSplit(
        target: RenderTarget,
        clear: Boolean,
        guiScaledW: Float,
        guiScaledH: Float,
        aboveTarget: RenderTarget?,
        blurRegions: List<BlurRegion>,
        backdropBlur: org.academy.api.client.render.post.BackdropBlurEngine? = null
    ) {
        val environment = UiEnvironment.get()
        val effectiveScale = environment.guiScale

        val commands = commandList.getAndSet(null)
        if (commands.isNullOrEmpty()) return

        val preparedCommands = prepareItemCommands(commands, effectiveScale)

        // 首版 C-stun: 25.5 的模糊/compositor 双缓冲后处理暂跳过, 一律直接画到 target.
        drawPrepared(target, preparedCommands, clear, guiScaledW, guiScaledH, effectiveScale)
    }

    @RenderThread
    private fun drawSegments(
        target: RenderTarget,
        aboveTarget: RenderTarget,
        segments: List<Pair<MutableList<SubmittedCommand>, List<BlurRegion>>>,
        clear: Boolean,
        guiScaledW: Float,
        guiScaledH: Float,
        effectiveScale: Float,
        backdropBlur: org.academy.api.client.render.post.BackdropBlurEngine
    ) {
        if (segments.isEmpty()) return

        // 首版 C-stun: 模糊/compositor 后处理跳过, 把所有有命令的段直接画到 target.
        val merged = ArrayList<SubmittedCommand>()
        for ((segmentCommands, _) in segments) merged.addAll(segmentCommands)
        if (merged.isNotEmpty()) {
            drawPrepared(target, merged, clear, guiScaledW, guiScaledH, effectiveScale)
        }
    }

    @RenderThread
    private fun drawPrepared(
        target: RenderTarget,
        commands: MutableList<SubmittedCommand>,
        clear: Boolean,
        guiScaledW: Float,
        guiScaledH: Float,
        effectiveScale: Float
    ) {
        // 1.21.1 无 GPU 延迟渲染目标/命令编码; 直接在当前绑定的 framebuffer 上即时绘制.
        if (clear) {
            RenderSystem.clearColor(0f, 0f, 0f, 0f)
            RenderSystem.clear(16384, Minecraft.ON_OSX)
        }

        if (commands.isEmpty()) return

        // 每趟次独立分配领地, 趟末统一释放: 不与其他 UI 层/渲染子系统共享原生缓冲,
        // 避免 MSDF 字体纹理由后台线程上传期间, 渲染线程仍向同一已释放/复用的缓冲区写入顶点.
        val buffer = ByteBufferBuilder(786432)
        try {
            val meshesToDraw = BatchProcessor.process(
                commands,
                object : UboUploader {
                    override fun <T : DynamicUniform> upload(payload: UniformPayload<T>): ByteBuffer {
                        return payload.toByteBuffer()
                    }
                },
                buffer
            )

            commandExecutor.execute(meshesToDraw, effectiveScale)
        } finally {
            buffer.close()
        }
    }

    companion object {
        /**
         * 按模糊区域的 [BlurRegion.commandIndex] 将命令列表切分为多个段喵.
         * 返回 List<Pair<该段的命令, 该段需要模糊的区域>>.
         * 相邻同一 commandIndex 的区域归入同一段.
         */
        fun splitCommandsForBlurByIndex(
            commands: List<SubmittedCommand>,
            regions: List<BlurRegion>
        ): List<Pair<MutableList<SubmittedCommand>, List<BlurRegion>>> {
            if (regions.isEmpty()) return listOf(commands.toMutableList() to emptyList())

            val sortedRegions = regions.sortedBy { it.commandIndex }
            val result = mutableListOf<Pair<MutableList<SubmittedCommand>, List<BlurRegion>>>()
            var prevIndex = 0
            var i = 0

            while (i < sortedRegions.size) {
                val region = sortedRegions[i]
                val idx = region.commandIndex

                if (idx > prevIndex) {
                    result.add(commands.subList(prevIndex, idx).toMutableList() to emptyList())
                }

                val sameIndex = mutableListOf<BlurRegion>()
                while (i < sortedRegions.size && sortedRegions[i].commandIndex == idx) {
                    sameIndex.add(sortedRegions[i])
                    i++
                }
                result.add(mutableListOf<SubmittedCommand>() to sameIndex)
                prevIndex = idx
            }

            if (prevIndex < commands.size) {
                result.add(commands.subList(prevIndex, commands.size).toMutableList() to emptyList())
            }

            return result
        }
    }

    @RenderThread
    private fun prepareItemCommands(
        commands: MutableList<SubmittedCommand>,
        effectiveScale: Float
    ): MutableList<SubmittedCommand> {
        // 1.21.1 移植: 所有物品统一改用原版 GuiGraphics.renderItem 即时绘制 (支持 BEWLR 外观
        // 与物品栏式提示框), 故整体从顶点批处理中剔除, 由 ScreenDispatcher.renderPost 逐控件绘制.
        return commands.filterNotTo(mutableListOf()) { submitted -> submitted.command is ItemStackDrawCommand }
    }

    open fun generateCommands(
        context: Canvas, rootWidget: WidgetContainer, mouseX: Double, mouseY: Double, partialTick: Float
    ) {
        context.pose().pushPose()
        run {
            context.pose().translate(rootWidget.x, rootWidget.y)
            context.pose().translate(rootWidget.translationX, rootWidget.translationY)
            rootWidget.render(context)
        }
        context.pose().popPose()
    }

    @RenderThread
    private fun initOnRenderThread() {
        // 1.21.1 即时渲染无需 GPU UBO/投影缓冲初始化.
    }

    fun close() {
        if (closing.get() || closed.get()) return
        closing.set(true)
        runOnRenderThread { this.closeOnRenderThread() }
    }

    fun closeOnRenderThread() {
        commandExecutor.close()
        lastBlurRegions = emptyList()
        performedRoot = null
        closed.set(true)
        closing.set(false)
    }
}
