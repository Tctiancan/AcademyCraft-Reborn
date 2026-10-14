package org.academy.api.client.gui.command

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.world.item.ItemStack
import net.neoforged.neoforge.client.model.data.ModelData
import org.academy.api.client.gui.render.VertexWriter
import org.academy.api.client.render.Render
import org.academy.api.client.render.TextureBinding
import org.academy.api.client.render.texture.FilterMode
import org.academy.api.client.render.texture.GpuSampler
import org.academy.api.client.render.texture.GpuTextureView
import org.academy.api.client.render.texture.SamplerCache

/**
 * 移植自 26.2 的 `ItemStackDrawCommand`.
 *
 * 26.2 中把物品经 `GuiItemAtlas` (GPU 离屏图集) 预渲染后, 经 [resolve] 转为带 UV 的
 * [PosTexColorRectDrawCommand] 四边形采样; 1.21.1 的物品图标已存在于标准图集纹理
 * (`minecraft:atlas/gui` 等), 由 [net.minecraft.client.resources.model.BakedModel.getParticleIcon]
 * 给出含图集路径与归一化 UV 的 [TextureAtlasSprite], 可直接走顶点批处理管线上色,
 * 无需自建离屏图集目标.
 */
class ItemStackDrawCommand(
    val itemStack: ItemStack,
    val width: Float,
    val height: Float,
    val alpha: Float
) : DrawCommand(Render.RenderPipelines.IMAGE_PREMULTIPLIED_ALPHA, emptyList(), emptyList()) {
    override fun generateVertices(writer: VertexWriter, pose: PoseStack.Pose, alphaMul: Float) {
        // 占位: 真顶点由 [resolve] 生成的 [PosTexColorRectDrawCommand] 产生, 本命令不应直接参与批次.
        throw IllegalStateException("ItemStackDrawCommand must be resolved by UiContext before batching in 1.21.1")
    }

    /**
     * 解析当前物品为可采样的四边形绘制命令. 依据 1.21.1 的物品模型粒子图标得到
     * [TextureAtlasSprite] (含图集纹理路径与归一化 UV), 再包装为预乘 alpha 的绘图命令.
     *
     * @return 可参与批处理的命令; 模型/图集尚未就绪 (如世界加载中) 时返回 null, 由调用方略过该物品.
     */
    fun resolve(): PosTexColorRectDrawCommand? {
        val mc = Minecraft.getInstance()
        val level = mc.level ?: return null
        val model = try {
            mc.itemRenderer.getModel(itemStack, level, null, 0)
        } catch (_: Throwable) {
            return null
        }
        // 物品粒子图标即 GUI 图标纹理所在 Sprite.
        val sprite = try {
            model.getParticleIcon(ModelData.EMPTY)
        } catch (_: Throwable) {
            return null
        }
        return ItemAtlasDrawCommand(
            width,
            height,
            sprite,
            alpha,
            listOf(TextureBinding("Sampler0", GpuTextureView.of(sprite.atlasLocation()), DEFAULT_SAMPLER))
        )
    }

    private class ItemAtlasDrawCommand(
        width: Float,
        height: Float,
        sprite: TextureAtlasSprite,
        alpha: Float,
        textures: List<TextureBinding>
    ) : PosTexColorRectDrawCommand(
        Render.RenderPipelines.IMAGE_PREMULTIPLIED_ALPHA,
        width,
        height,
        sprite.getU0(),
        sprite.getV0(),
        sprite.getU1(),
        sprite.getV1(),
        alpha,
        alpha,
        alpha,
        alpha,
        textures,
        emptyList()
    ) {
        // 物品图标为预乘 alpha: 淡出时 rgb 须随 alpha 一起缩放, 保证边缘不见黑.
        override val premultiplied: Boolean get() = true
    }

    private companion object {
        val DEFAULT_SAMPLER: GpuSampler =
            SamplerCache.INSTANCE.getClampToEdge(FilterMode.NEAREST)
    }
}
