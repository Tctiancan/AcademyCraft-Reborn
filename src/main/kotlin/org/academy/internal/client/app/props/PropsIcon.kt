package org.academy.internal.client.app.props

import net.minecraft.resources.ResourceLocation
import org.academy.AcademyCraft

object PropsIcon {
    val LOCATION: ResourceLocation = AcademyCraft.academy("textures/gui/app/props/icon.png")

    /** Kept for the existing client bootstrap; the icon is now a normal resource texture. */
    fun init() = Unit
}
