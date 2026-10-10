package org.academy.api.client.gui.drawable

import net.minecraft.resources.ResourceLocation
import org.academy.api.client.gui.command.ImageDrawCommand
import org.academy.api.client.gui.render.Canvas
import org.academy.api.client.gui.texture.GpuTextureViewSource
import org.academy.api.client.gui.texture.IdentifierTextureSource
import org.academy.api.client.gui.texture.TextureSource
import org.academy.api.client.gui.widget.Widget
import org.academy.api.client.render.texture.FilterMode
import org.academy.api.client.render.texture.GpuSampler
import org.academy.api.client.render.texture.GpuTextureView

open class TextureDrawable : Drawable {
    private val textureSource: TextureSource?

    var sampler: GpuSampler = org.academy.api.client.render.texture.SamplerCache.INSTANCE.getClampToEdge(FilterMode.NEAREST)

    var tintColor: Int = -0x1

    constructor(textureLocation: ResourceLocation?) {
        textureSource = textureLocation?.let { IdentifierTextureSource(it) }
    }

    constructor(texture: GpuTextureView?) {
        textureSource = GpuTextureViewSource(texture)
    }

    override fun draw(context: Canvas, widget: Widget) {
        val source = textureSource ?: return
        val view = source.getTextureView()
        if (view == null || view.isClosed) return

        drawPaddedContent(context, widget, tintColor) { ctx, width, height, r, g, b, alpha ->
            ctx.submit(ImageDrawCommand(view, sampler, width, height, 0f, 0f, 1f, 1f, r, g, b, alpha))
        }
    }
}
