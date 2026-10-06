package org.academy.api.client.gui.screen

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import org.academy.api.client.gui.event.CharTypedEvent
import org.academy.api.client.gui.event.EventType
import org.academy.api.client.gui.event.MouseEvent
import org.academy.api.client.gui.event.ScrollEvent
import org.academy.api.client.gui.imgui.ImGuiUtilApi
import org.academy.api.client.gui.widget.FrameLayoutWidget

abstract class UiScreen protected constructor(title: Component) : Screen(title), RenderRoot {
    override val root: FrameLayoutWidget = FrameLayoutWidget()

    override fun init() {
        ImGuiUtilApi.clearEventsQueue()

        root.name = "root"
        root.clearChildren()

        onInit()

        if (!root.isAttached()) root.dispatchAttached()
    }

    protected abstract fun onInit()

    /**
     * 1.21.1 迁移桩：26.2 的 `extractRenderState(GuiGraphicsExtractor, ...)` 抽取契约在此声明为 open 空实现，
     * 供需要向下兼容 @Override 的 Java 屏幕使用。图形参数已从 [GuiGraphics] 传入（26.2 的
     * `com.mojang.blaze3d.GuiGraphicsExtractor` 在 1.21.1 不存在）。内容绘制仍交由 `render` 回调。
     */
    open fun extractRenderState(graphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
    }

    open fun extractBackground(graphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        // 1.21.1: Academy UI 已改由 ScreenDispatcher 经 UiContext 直接绘制到主 framebuffer,
        // 此处仅保留原版屏幕背景. 原 25.5 的离屏渲染目标背景 blit (GuiGraphicsExtractor) 已移除.
        super.render(guiGraphics, mouseX, mouseY, partialTick)
    }

    override fun removed() {
        super.removed()
        if (root.isAttached()) root.dispatchDetached()
    }

    override fun mouseMoved(mouseX: Double, mouseY: Double) {
        if (ImGuiUtilApi.wantCaptureMouse()) return

        val event: MouseEvent = MouseEvent.createMoveEvent(mouseX, mouseY)
        root.dispatchEvent(event)
        super.mouseMoved(mouseX, mouseY)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (ImGuiUtilApi.wantCaptureMouse()) return true

        val event: MouseEvent = MouseEvent.createPressEvent(mouseX, mouseY, button)
        root.dispatchEvent(event)

        return event.isConsumed || super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (ImGuiUtilApi.wantCaptureMouse()) return true

        val event: MouseEvent = MouseEvent.createReleaseEvent(mouseX, mouseY, button)
        root.dispatchEvent(event)
        return event.isConsumed || super.mouseReleased(mouseX, mouseY, button)
    }

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, dragX: Double, dragY: Double): Boolean {
        if (ImGuiUtilApi.wantCaptureMouse()) return true

        val event: MouseEvent = MouseEvent.createDragEvent(mouseX, mouseY, button, dragX, dragY)
        root.dispatchEvent(event)
        return event.isConsumed || super.mouseDragged(mouseX, mouseY, button, dragX, dragY)
    }

    override fun mouseScrolled(
        mouseX: Double,
        mouseY: Double,
        horizontalAmount: Double,
        verticalAmount: Double
    ): Boolean {
        if (ImGuiUtilApi.wantCaptureMouse()) return true

        val event = ScrollEvent(mouseX, mouseY, verticalAmount)
        root.dispatchEvent(event)
        return event.isConsumed || super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (ImGuiUtilApi.wantCaptureKeyboard()) return true

        val event =
            org.academy.api.client.gui.event.KeyEvent(EventType.KEY_PRESSED, keyCode, scanCode, modifiers)
        root.dispatchEvent(event)
        return event.isConsumed || super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun charTyped(codePoint: Char, modifiers: Int): Boolean {
        if (ImGuiUtilApi.wantCaptureKeyboard()) return true

        val event = CharTypedEvent(codePoint.toInt())
        root.dispatchEvent(event)
        return event.isConsumed || super.charTyped(codePoint, modifiers)
    }
}
