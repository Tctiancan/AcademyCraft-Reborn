package org.academy.api.client.gui.text.model

import net.minecraft.resources.ResourceLocation

data class TextShapingOptions(
    val letterSpacing: Float = 0f,
    val textScaleX: Float = 1f,
    val lineSpacingMultiplier: Float = 1f,
    val lineSpacingExtra: Float = 0f,
    val includeFontPadding: Boolean = true,
    val preferredFont: ResourceLocation? = null,
    val fontStyle: FontStyle = FontStyle.NORMAL
) {
    companion object {
        const val DEFAULT_SIZE: Float = 8f

        val DEFAULT: TextShapingOptions = TextShapingOptions()
    }
}
