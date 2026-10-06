package org.academy.api.client.gui.widget

import net.minecraft.client.Minecraft
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth
import org.academy.api.client.gui.event.MouseEvent
import org.academy.api.client.gui.render.Canvas

open class ParallaxImageWidget(texture: ResourceLocation) : ImageWidget(texture) {
    private var parallaxFactorX: Float = 0.5f
    private var parallaxFactorY: Float = 0.5f

    private var imageToViewRatioWidth: Float = 0.9f
    private var imageToViewRatioHeight: Float = 0.9f
    var parallaxEnabled: Boolean = true

    override fun render(context: Canvas) {
        if (!parallaxEnabled) {
            val uOffset = (1.0f - imageToViewRatioWidth) / 2.0f
            val vOffset = (1.0f - imageToViewRatioHeight) / 2.0f
            setUv(uOffset, vOffset, uOffset + imageToViewRatioWidth, vOffset + imageToViewRatioHeight)
            super.render(context)
            return
        }

        val anchorX = getAbsoluteX() + width / 2.0f
        val anchorY = getAbsoluteY() + height / 2.0f

        val mc: Minecraft = Minecraft.getInstance()
        val motionX: Float
        val motionY: Float
        val mh = mc.mouseHandler
        val w = mc.window
        // 1.21.1: MouseHandler 无 26.2 的 getScaledXPos/getScaledYPos; xpos()/ypos() 返回缩放前像素坐标,
        // 乘以 guiScale 逆即 GUI 坐标, 等价 26.2 的 getScaledXPos(窗口).
        val mouseX = (mh.xpos() / w.guiScale).toFloat()
        val mouseY = (mh.ypos() / w.guiScale).toFloat()

        val deviationX = ((mouseX - anchorX) / anchorX).toFloat()
        val deviationY = ((mouseY - anchorY) / anchorY).toFloat()

        motionX = Mth.clamp(deviationX * parallaxFactorX, -1.0f, 1.0f)
        motionY = Mth.clamp(deviationY * parallaxFactorY, -1.0f, 1.0f)

        val maxUOffset = 1.0f - imageToViewRatioWidth
        val maxVOffset = 1.0f - imageToViewRatioHeight

        val uOffset = (motionX + 1.0f) / 2.0f * maxUOffset
        val vOffset = (motionY + 1.0f) / 2.0f * maxVOffset

        setUv(uOffset, vOffset, uOffset + imageToViewRatioWidth, vOffset + imageToViewRatioHeight)

        super.render(context)
    }

    override fun onMouseMoved(event: MouseEvent) {
        super.onMouseMoved(event)
        invalidate()
    }

    fun setParallaxFactor(parallaxFactorX: Float, parallaxFactorY: Float): ParallaxImageWidget {
        this.parallaxFactorX = parallaxFactorX
        this.parallaxFactorY = parallaxFactorY
        return this
    }

    fun setImageToViewRatio(imageToViewRatioWidth: Float, imageToViewRatioHeight: Float): ParallaxImageWidget {
        this.imageToViewRatioWidth = Mth.clamp(imageToViewRatioWidth, 0f, 1f)
        this.imageToViewRatioHeight = Mth.clamp(imageToViewRatioHeight, 0f, 1f)
        return this
    }

    fun setParallaxEnabled(enabled: Boolean): ParallaxImageWidget {
        parallaxEnabled = enabled
        return this
    }
}
