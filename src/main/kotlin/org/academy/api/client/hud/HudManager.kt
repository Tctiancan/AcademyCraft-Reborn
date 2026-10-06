package org.academy.api.client.hud

import net.minecraft.client.Minecraft
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import org.academy.AcademyCraft
import org.academy.AcademyCraftClient
import org.academy.api.client.hud.ability.AbilityInfoHud
import org.academy.api.client.hud.ability.ControlledTargetsHud
import org.academy.api.client.hud.ability.ToggleStatusHud
import org.academy.api.client.hud.terminal.TerminalHud
import org.academy.api.client.gui.imgui.ImGuiUIDebugger
import org.academy.api.client.gui.widget.WidgetContainer
import org.academy.api.client.vanilla.MainLoopEvent
import org.academy.api.common.profiler.AcademyProfiler
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
        // C-stun: 1.21.1 首版略去世界级 HUD 合成/模糊后处理.
        // 原实现依赖 26.2 专属 GPU API (Render.Buffers.getResourcePool /
        // RenderTargetDescriptor / GpuFormat / Render.runBlitPass / getColorTextureView 等),
        // 在 1.21.1 均已被移除或重构; 保留方法入口以便后续迁移.
    }
}
