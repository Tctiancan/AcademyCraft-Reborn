package org.academy.api.client.gui.screen

import com.mojang.blaze3d.pipeline.RenderTarget
import net.minecraft.client.Minecraft
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.client.event.ScreenEvent
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.GameShuttingDownEvent
import org.academy.AcademyCraft
import org.academy.api.client.gui.environment.UiEnvironment
import org.academy.api.client.gui.render.UiContext
import org.academy.api.client.thread.RenderThread

/**
 * 1.21.1 移植版 `ScreenDispatcher`, 移植自 25.5 离屏 GPU 目标 + 双缓冲后处理版本.
 *
 * 首版 C-stun / 简化:
 * - 移除 25.5 的离屏 [TextureTarget]/[aboveTarget] 与模糊/compositor 后处理,
 *   改为通过 [UiContext] 在当前主 framebuffer 上即时绘制.
 * - 26.2 时由 MixinGameRenderer.post(RenderLoopEvent) 触发提交绘制; 该 mixin 依赖
 *   26.2 世界级渲染类型 (SubmitNodeStorage/CameraRenderState 等, 已 C-stun) 无法在 1.21.1 编译,
 *   故改用标准的 `ScreenEvent.Render.Post` (紧跟 `Render.Pre` 采集后触发) 提交绘制,
 *   保证 "先 perform 采集命令, 后 upload 即时绘制" 的顺序且无需 mixin.
 */
class ScreenDispatcher private constructor() {
    private val uiContext = UiContext()

    @SubscribeEvent
    fun onScreenRenderPre(event: ScreenEvent.Render.Pre) {
        val screen = event.getScreen()
        if (screen !is RenderRoot) return
        uiContext.perform(
            screen.root,
            event.getMouseX().toDouble(),
            event.getMouseY().toDouble(),
            event.getPartialTick()
        )
    }

    @SubscribeEvent
    fun onScreenRenderPost(event: ScreenEvent.Render.Post) {
        val mc = Minecraft.getInstance()
        val screen = event.getScreen()
        if (screen !is RenderRoot) return
        val env = UiEnvironment.get()
        uiContext.upload(
            mc.getMainRenderTarget(),
            false,
            env.physicalWidth / env.guiScale,
            env.physicalHeight / env.guiScale
        )
    }

    @SubscribeEvent
    fun onShutdown(@Suppress("unused") event: GameShuttingDownEvent) {
        uiContext.close()
    }

    companion object {
        val logger = AcademyCraft.getLogger()
        private lateinit var INSTANCE: ScreenDispatcher

        @RenderThread
        fun init() {
            INSTANCE = ScreenDispatcher()
            NeoForge.EVENT_BUS.register(INSTANCE)
        }

        /**
         * 1.21.1 兼容存取器: 离屏渲染目标已移除, 返回主渲染目标 (当前 Academy UI 的直接绘制目标).
         */
        fun getRenderTarget(): RenderTarget {
            return Minecraft.getInstance().getMainRenderTarget()
        }
    }
}
