package org.academy.api.client.gui.widget

import net.minecraft.resources.ResourceLocation
import org.academy.api.client.gui.command.DrawCommand
import org.academy.api.client.gui.command.MonochromeImageDrawCommand
import org.academy.api.client.render.texture.GpuSampler
import org.academy.api.client.render.texture.GpuTextureView

class MonochromeImageWidget(texture: ResourceLocation) : ImageWidget(texture) {
    override fun generateDrawCommand(
        texture: GpuTextureView, sampler: GpuSampler,
        width: Float, height: Float,
        u0: Float, v0: Float, u1: Float, v1: Float, u2: Float, v2: Float, u3: Float, v3: Float,
        red: Float, green: Float, blue: Float, alpha: Float
    ): DrawCommand {
        return MonochromeImageDrawCommand(
            texture, sampler, width, height, u0, v0, u1, v1, u2, v2, u3, v3, red, green, blue, alpha
        )
    }
}
