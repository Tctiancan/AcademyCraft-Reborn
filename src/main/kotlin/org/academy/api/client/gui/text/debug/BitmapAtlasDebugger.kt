package org.academy.api.client.gui.text.debug

import com.mojang.blaze3d.systems.RenderSystem
import org.academy.AcademyCraft
import org.academy.api.client.gui.text.atlas.AtlasPage
import org.academy.api.client.gui.text.glyph.bitmap.BitmapAtlas
import java.nio.file.Path

object BitmapAtlasDebugger {
    private val logger = AcademyCraft.getLogger()

    fun dumpAtlas(atlas: BitmapAtlas, outputDir: Path, fileNamePrefix: String): List<Path> {
        RenderSystem.assertOnRenderThread()
        val pages = atlas.getPages()
        val written = ArrayList<Path>(pages.size)
        for (pageIndex in pages.indices) {
            written.add(dumpPage(pages[pageIndex], pageIndex, outputDir, fileNamePrefix))
        }
        return written
    }

    private fun dumpPage(page: AtlasPage, pageIndex: Int, outputDir: Path, fileNamePrefix: String): Path {
        val file = outputDir.resolve("$fileNamePrefix$pageIndex.png")
        // C-stun: 1.21.1 无 26.2 的 RenderDevice/GpuBuffer 回读管线, 位图图集调试导出暂不执行.
        logger.warn("Bitmap atlas debug dump is disabled in 1.21.1 port (page {})", pageIndex)
        return file
    }
}
