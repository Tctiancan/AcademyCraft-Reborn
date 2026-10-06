package org.academy.api.client.gui.command

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.world.item.ItemStack
import org.academy.api.client.gui.render.VertexWriter
import org.academy.api.client.render.Render

/**
 * 首版 C-stun: 1.21.1 无 `GuiItemAtlas` 物品图集与 `TrackingItemStackRenderState`
 * (均为 26.2 新增的图集/模型解析管线). 物品渲染命令在 [org.academy.api.client.gui.render.UiContext]
 * 的 `prepareItemCommands` 里被整组跳过, 不进入批次渲染, 故此处仅保留占位行为.
 */
class ItemStackDrawCommand(
    val itemStack: ItemStack,
    val width: Float,
    val height: Float,
    val alpha: Float
) : DrawCommand(Render.RenderPipelines.IMAGE_PREMULTIPLIED_ALPHA, emptyList(), emptyList()) {
    override fun generateVertices(writer: VertexWriter, pose: PoseStack.Pose, alphaMul: Float) {
        error("ItemStackDrawCommand is c-stunned and must never be batched in 1.21.1")
    }
}
