package org.academy.api.client.gui.text

import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.PreparableReloadListener
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.util.profiling.ProfilerFiller
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent
import org.academy.AcademyCraft
import org.academy.api.client.gui.text.atlas.GlyphSignal
import org.academy.api.client.gui.text.font.AwtFaceCache
import org.academy.api.client.gui.text.font.FontRepository
import org.academy.api.client.gui.text.glyph.GlyphPrewarm
import org.academy.api.client.gui.text.glyph.bitmap.BitmapFacePool
import org.academy.api.client.gui.text.glyph.bitmap.GlyphStrikeCache
import org.academy.api.client.gui.text.glyph.msdf.MsdfGlyphCache
import org.academy.api.client.gui.text.shape.ShapingCache
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor

private val LOGGER = AcademyCraft.getLogger()

@EventBusSubscriber(Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
object TextReloadListener : PreparableReloadListener {
    @SubscribeEvent
    fun onAddReloadListener(event: RegisterClientReloadListenersEvent) {
        event.registerReloadListener(this)
        LOGGER.info("[TextReloadListener] registered as '{}'", AcademyCraft.academy("font_loader"))
    }

    override fun reload(
        preparationBarrier: PreparableReloadListener.PreparationBarrier,
        resourceManager: ResourceManager,
        preparationsProfiler: ProfilerFiller,
        reloadProfiler: ProfilerFiller,
        backgroundExecutor: Executor,
        gameExecutor: Executor
    ): CompletableFuture<Void> {
        val resources = resourceManager.listResources("fonts") { FontRepository.isFont(it) }.keys
        LOGGER.info(
            "[TextReloadListener] reload start: discovered {} font resource(s) {}; fontsReady(before)={}",
            resources.size, resources, FontRepository.isFontsReady()
        )

        val futures = resources.map { loadFont(it, backgroundExecutor) }.toTypedArray()
        val all = CompletableFuture.allOf(*futures)
        all.whenComplete { _, error ->
            if (error != null) {
                LOGGER.error("[TextReloadListener] one or more fonts failed to load", error)
            }
        }
        return all.thenCompose { future ->
            FontRepository.setFontSearchOrder(resources.toList())

            GlyphStrikeCache.clear()
            BitmapFacePool.clear()
            MsdfGlyphCache.clear()
            AwtFaceCache.clear()
            ShapingCache.clear()
            LOGGER.info("[TextReloadListener] face caches cleared (strike/bitmapFace/msdf/awt/shaping)")

            FontRepository.markFontsReady()
            GlyphSignal.bump()
            GlyphPrewarm.onFontsReady()
            LOGGER.info("[TextReloadListener] reload prepared; state={}", FontRepository.snapshot())

            preparationBarrier.wait(future)
        }
    }

    private fun loadFont(resource: ResourceLocation, taskExecutor: Executor): CompletableFuture<Void> =
        CompletableFuture.runAsync({
            try {
                LOGGER.info("[TextReloadListener] loading font face '{}'", resource)
                FontRepository.loadFont(resource)
            } catch (e: Exception) {
                LOGGER.error("[TextReloadListener] failed to load font face '{}'", resource, e)
                throw e
            }
        }, taskExecutor)
}
