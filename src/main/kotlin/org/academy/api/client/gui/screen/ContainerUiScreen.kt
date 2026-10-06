package org.academy.api.client.gui.screen

import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack
import org.academy.AcademyCraft
import org.academy.api.client.gui.animation.EasingFunctions
import org.academy.api.client.gui.animation.ObjectAnimator
import org.academy.api.client.gui.drawable.StateListDrawable
import org.academy.api.client.gui.drawable.TextureDrawable
import org.academy.api.client.gui.event.CharTypedEvent
import org.academy.api.client.gui.event.EventType
import org.academy.api.client.gui.event.MouseEvent
import org.academy.api.client.gui.event.ScrollEvent
import org.academy.api.client.gui.imgui.ImGuiUtilApi
import org.academy.api.client.gui.layout.Orientation
import org.academy.api.client.gui.layout.SizeMode
import org.academy.api.client.gui.widget.*
import org.academy.api.client.resources.R

abstract class ContainerUiScreen<T : AbstractContainerMenu> protected constructor(
    menu: T,
    playerInventory: Inventory,
    title: Component
) : AbstractContainerScreen<T>(menu, playerInventory, title), RenderRoot {
    override val root: FrameLayoutWidget = FrameLayoutWidget()

    var isHandleContainer: Boolean = true

    /** 文本输入获得焦点时，容器不再消费按键/鼠标事件（IME 优先）。 */
    private val handleContainer: Boolean
        get() = isHandleContainer && !TextInputFocus.isActiveWithin(root)
    var isRenderInventory: Boolean = true
        set(renderInventory) {
            field = renderInventory
            invVisibleSetter.invoke(renderInventory)
        }
    private var invHeightSupplier: () -> Float = { 1f }
    private var invTranslationYSupplier: () -> Float = { 1f }
    private var invVisibleSetter = { _: Boolean -> }

    override fun init() {
        super.init()
        ImGuiUtilApi.clearEventsQueue()

        root.name = "root"
        root.clearChildren()

        val finalHeight = 187f
        val duration = 600L

        val main = LinearLayoutWidget()
        main.orientation = Orientation.HORIZONTAL
        main.layoutParams = FrameLayoutWidget.LayoutParams()
            .widthMode(SizeMode.WRAP_CONTENT)
            .heightMode(SizeMode.WRAP_CONTENT)
            .margin((leftPos - 16).toFloat(), (topPos - 22).toFloat(), 0f, 0f)

        root.addChild("main", main)
        main.startAnimation(
            ObjectAnimator.ofFloat({ alpha -> main.alpha = alpha }, 0f, 1.0f)
                .setDuration(duration)
                .setInterpolator(EasingFunctions.EASE_OUT_EXPO)
        )
        main.startAnimation(
            ObjectAnimator.ofFloat({ height -> main.height = height }, 0f, finalHeight)
                .setDuration(duration)
                .setInterpolator(EasingFunctions.EASE_OUT_EXPO)
        )
        run {
            val pageButtons = RadioGroupWidget()
            pageButtons.orientation = Orientation.VERTICAL
            pageButtons.layoutParams = LinearLayoutWidget.LayoutParams()
                .width(16f)
                .heightMode(SizeMode.WRAP_CONTENT)

            main.addChild("radio_group_page_button", pageButtons)
            val invButton: RadioButtonWidget
            run {
                invButton = createButton(R.textures.gui.icon.icon_inv)
                invButton.layoutParams = WidgetContainer.LayoutParams()
                    .widthMode(SizeMode.MATCH_PARENT)
                    .height(16f)

                pageButtons.addChild("inv", invButton)
                pageButtons.selectButton(invButton)
            }

            val content = FrameLayoutWidget()
            content.layoutParams = LinearLayoutWidget.LayoutParams()
                .width(imageWidth.toFloat())
                .height(187f)

            main.addChild("content", content)
            run {
                val invPage = FrameLayoutWidget()
                invTranslationYSupplier = { invPage.translationY }
                invHeightSupplier = { invPage.height }

                invVisibleSetter = { visible ->
                    invPage.visibility = (if (visible) Widget.Visibility.VISIBLE else Widget.Visibility.GONE)
                }
                invPage.layoutParams = FrameLayoutWidget.LayoutParams()
                    .widthMode(SizeMode.MATCH_PARENT)
                    .heightMode(SizeMode.MATCH_PARENT)

                content.addChild("page_inv", invPage)
                invPage.startAnimation(ObjectAnimator.ofFloat({ height ->
                    invPage.height = height

                }, 0f, finalHeight).setDuration(duration).setInterpolator(EasingFunctions.EASE_OUT_EXPO))
                run {
                    val back = BlendQuadWidget()
                    back.layoutParams = FrameLayoutWidget.LayoutParams()
                        .widthMode(SizeMode.MATCH_PARENT)
                        .heightMode(SizeMode.MATCH_PARENT)
                    back.alpha = 0.5f
                    invPage.addChild("back", back)

                    val inv = ImageWidget(R.textures.gui.element.ui_inventory)
                    inv.layoutParams = FrameLayoutWidget.LayoutParams()
                        .widthMode(SizeMode.MATCH_PARENT)
                        .heightMode(SizeMode.MATCH_PARENT)
                    invPage.addChild("inv", inv)
                    onInit(pageButtons, invButton, content, invPage)
                }
            }
        }

        if (!root.isAttached()) {
            root.dispatchAttached()
        }
    }

    protected abstract fun onInit(
        pageButtons: RadioGroupWidget,
        invButton: RadioButtonWidget,
        content: FrameLayoutWidget,
        invPage: FrameLayoutWidget
    )

    protected fun createButton(textureLocation: ResourceLocation): RadioButtonWidget {
        val widget = RadioButtonWidget()
        val defaultDrawable = TextureDrawable(textureLocation)
        defaultDrawable.tintColor = 0xFFE6E6E6.toInt()

        val hoveredDrawable = TextureDrawable(textureLocation)
        hoveredDrawable.tintColor = -0x1

        val sld = StateListDrawable()
        sld.setDefault(defaultDrawable)
        sld.addState(Widget.FOCUSED, hoveredDrawable)
        sld.addState(Widget.SELECTED, hoveredDrawable)
        sld.addState(Widget.HOVERED, hoveredDrawable)
        sld.addState(Widget.PRESSED, hoveredDrawable)

        widget.background = sld
        return widget
    }

    override fun renderBg(guiGraphics: GuiGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        // 1.21.1: 首版以 C-stun 略去原生容器背景绘制; Academy UI 背景由自身绘制系统直接提交到主 framebuffer.
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        // 1.21.1: 由原生 AbstractContainerScreen.render 负责 背景/槽位/标签/手持物品/悬浮提示.
        // 原 25.5 的离屏 Academy 渲染目标背景 blit 已移除 (Academy UI 改在主 framebuffer 直接绘制);
        // 物品栏动画缩放 (invHeightSupplier/invTranslationYSupplier) 首版以 C-stun 略去.
        super.render(guiGraphics, mouseX, mouseY, partialTick)
    }

    override fun removed() {
        super.removed()
        if (root.isAttached()) {
            root.dispatchDetached()
        }
    }

    override fun renderSlotContents(
        graphics: GuiGraphics,
        itemstack: ItemStack,
        slot: Slot,
        itemCount: String?
    ) {
        val pose = graphics.pose()
        pose.pushPose()

        pose.translate(slot.x.toFloat(), slot.y.toFloat(), 0f)
        pose.translate(8.0f, 8.0f, 0f)

        pose.scale(2 / 3f, 2 / 3f, 1.0f)

        pose.translate(-8.0f, -8.0f, 0f)
        pose.translate(-slot.x.toFloat(), -slot.y.toFloat(), 0f)

        super.renderSlotContents(graphics, itemstack, slot, itemCount)

        pose.popPose()
    }

    override fun renderLabels(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int) {
    }

    override fun mouseMoved(mouseX: Double, mouseY: Double) {
        if (ImGuiUtilApi.wantCaptureMouse()) return

        val event: MouseEvent = MouseEvent.createMoveEvent(mouseX, mouseY)
        root.dispatchEvent(event)
        super.mouseMoved(mouseX, mouseY)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        if (ImGuiUtilApi.wantCaptureMouse()) return true

        val event = ScrollEvent(mouseX, mouseY, scrollY)
        root.dispatchEvent(event)
        val rootResult = event.isConsumed

        var superResult = false
        if (this.isHandleContainer) superResult = super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)

        return rootResult || superResult
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (ImGuiUtilApi.wantCaptureMouse()) return true

        val event: MouseEvent = MouseEvent.createReleaseEvent(mouseX, mouseY, button)
        root.dispatchEvent(event)
        val rootResult = event.isConsumed

        var superResult = false
        if (this.isHandleContainer) superResult = super.mouseReleased(mouseX, mouseY, button)

        return rootResult || superResult
    }

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, dragX: Double, dragY: Double): Boolean {
        if (ImGuiUtilApi.wantCaptureMouse()) return true

        val event: MouseEvent = MouseEvent.createDragEvent(mouseX, mouseY, button, dragX, dragY)
        root.dispatchEvent(event)
        val rootResult = event.isConsumed

        var superResult = false
        if (this.isHandleContainer) superResult = super.mouseDragged(mouseX, mouseY, button, dragX, dragY)

        return rootResult || superResult
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (ImGuiUtilApi.wantCaptureMouse()) return true

        val event: MouseEvent = MouseEvent.createPressEvent(mouseX, mouseY, button)
        root.dispatchEvent(event)
        val rootResult = event.isConsumed

        var superResult = false
        if (this.isHandleContainer) superResult = super.mouseClicked(mouseX, mouseY, button)

        return rootResult || superResult
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (ImGuiUtilApi.wantCaptureKeyboard()) return true

        if (keyCode == InputConstants.KEY_F12) {
            AcademyCraft.DEBUG_UI = !AcademyCraft.DEBUG_UI
            return true
        }

        val event =
            org.academy.api.client.gui.event.KeyEvent(EventType.KEY_PRESSED, keyCode, scanCode, modifiers)
        root.dispatchEvent(event)
        if (event.isConsumed) return true
        if (keyCode == InputConstants.KEY_ESCAPE && shouldCloseOnEsc()) {
            onClose()
            return true
        }
        return this.handleContainer && super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun charTyped(codePoint: Char, modifiers: Int): Boolean {
        if (ImGuiUtilApi.wantCaptureKeyboard()) return true

        val event = CharTypedEvent(codePoint.toInt())
        root.dispatchEvent(event)
        return event.isConsumed || this.handleContainer && super.charTyped(codePoint, modifiers)
    }

    override fun hasClickedOutside(mouseX: Double, mouseY: Double, guiLeft: Int, guiTop: Int, mouseButton: Int): Boolean {
        return mouseX < guiLeft || mouseY < guiTop - 22 || mouseX >= guiLeft + imageWidth || mouseY >= guiTop + 187
    }
}
