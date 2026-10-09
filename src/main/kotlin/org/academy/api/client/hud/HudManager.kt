package org.academy.api.client.hud

import net.minecraft.client.Minecraft
import net.minecraft.resources.ResourceLocation
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.client.event.RenderGuiEvent
import org.academy.AcademyCraft
import org.academy.AcademyCraftClient
import org.academy.api.client.hud.ability.AbilityInfoHud
import org.academy.api.client.hud.ability.ControlledTargetsHud
import org.academy.api.client.hud.ability.ToggleStatusHud
import org.academy.api.client.hud.terminal.TerminalHud
import org.academy.api.client.gui.imgui.ImGuiUIDebugger
import org.academy.api.client.gui.widget.WidgetContainer
import org.academy.api.client.render.Render
import org.academy.api.client.render.texture.GpuTextureView
import org.academy.api.client.vanilla.MainLoopEvent
import org.academy.api.common.profiler.AcademyProfiler
import org.academy.internal.client.profiler.ProfilerClientHooks
import org.slf4j.Logger
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 负责管理本模组内置的 UI 帧缓冲区与 HUD 喵
 */
@EventBusSubscriber(Dist.CLIENT)
object HudManager {
    val logger: Logger = AcademyCraft.getLogger()

    fun initRender() {
    }

    private fun inspectableRoots(): List<Pair<String, WidgetContainer>> = listOf(
        "ability" to AbilityInfoHud.instance.root,
        "toggle_status" to ToggleStatusHud.instance.root,
        "mental_control" to ControlledTargetsHud.instance.root,
        "terminal" to TerminalHud.INSTANCE.root,
    )

    fun initMain() {
        TerminalHud.initMain()
        AbilityInfoHud.initMain()
        ToggleStatusHud.initMain()
        ControlledTargetsHud.initMain()
    }

    fun rebuildHudLayouts() {
        AbilityInfoHud.instance.rebuildLayout()
        ToggleStatusHud.instance.rebuildLayout()
        ControlledTargetsHud.instance.rebuildLayout()
    }

    fun resize(width: Int, height: Int) {
    }

    /**
     * 由 Main 线程调用喵, 生成 SubmittedCommand 列表喵
     */
    @SubscribeEvent
    fun onMainLoop(event: MainLoopEvent?) {
        if (!AcademyCraftClient.isRenderInitialized()) return

        val mc = Minecraft.getInstance()
        val w = mc.window
        val m = mc.mouseHandler
        // 1.21.1: MouseHandler 无 getScaledXPos/getScaledYPos, xpos()/ypos() 为物理像素, 除以 guiScale 即 GUI 坐标.
        val mouseX = (m.xpos() / w.guiScale).toDouble()
        val mouseY = (m.ypos() / w.guiScale).toDouble()
        // 1.21.1: Minecraft 无 deltaTracker, 改用 getTimer() 提供本帧 partialTick.
        val deltaPartialTick = mc.getTimer().getGameTimeDeltaPartialTick(false)
        TerminalHud.INSTANCE.perform(mouseX, mouseY, deltaPartialTick)
        AbilityInfoHud.instance.perform(mouseX, mouseY, deltaPartialTick)
        ToggleStatusHud.instance.perform(mouseX, mouseY, deltaPartialTick)
        ControlledTargetsHud.instance.perform(mouseX, mouseY, deltaPartialTick)
    }

    fun render() {
        // C-stun: 1.21.1 时 HUD 渲染已在 RenderGuiEvent.Post 挂接点即时绘制 (见 onRenderGui).
        // 26.2 原实现基于 GPU 离屏目标/模糊妥稿后处理 (Render.Buffers.getResourcePool /
        // RenderTargetDescriptor / GpuFormat / Render.runBlitPass / getColorTextureView 等),
        // 在 1.21.1 均已被移除或重构; 保留方法入口以便后续迁移.
    }

    /**
     * 1.21.1 的 HUD 渲染挂接点: 在原生 GUI/准星合成之后, 即时绘制各 HUD 控件树.
     * 迁移自 26.2 的 [render] 的调度逻辑, 保留了剖析/叠层/调试 HUD 等即时过程;
     * 终端 3D 透视变换+模板辉光+离屏合成 (Render.Buffers.getResourcePool /
     * RenderTargetDescriptor / GpuFormat / createRenderPass / BlurEffect / runBlitPass 等)
     * 依赖 26.2 专属 GPU 延迟渲染 API, 在 1.21.1 均已被移除或重构, 故按 2D 布局原样绘制 (C-stun).
     */
    @SubscribeEvent
    fun onRenderGui(@Suppress("unused") event: RenderGuiEvent.Post) {
        if (!AcademyCraftClient.isRenderInitialized()) return

        val mc = Minecraft.getInstance()
        val main = mc.getMainRenderTarget()
        val width = main.width
        val height = main.height

        AcademyProfiler.push("academy.hud.terminal")
        AcademyProfiler.popPush("academy.hud.ability")
        AbilityInfoHud.instance.render(main)
        AcademyProfiler.popPush("academy.hud.toggle")
        ToggleStatusHud.instance.render(main)
        ControlledTargetsHud.instance.render(main)
        TerminalHud.INSTANCE.render(
            width, height,
            GpuTextureView.of(ResourceLocation.fromNamespaceAndPath("academy", "terminal_hud")),
            GpuTextureView.of(ResourceLocation.fromNamespaceAndPath("academy", "terminal_hud")),
            AtomicBoolean()
        )
        AcademyProfiler.pop()

        ProfilerClientHooks.renderOverlay()
        if (ImGuiUIDebugger.enabled) {
            ImGuiUIDebugger.renderHud(main, inspectableRoots())
        }
    }
}
