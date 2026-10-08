package org.academy.api.client.gui.text.debug

import com.mojang.blaze3d.systems.RenderSystem
import org.academy.AcademyCraft
import org.academy.api.client.gui.text.atlas.AtlasPage
import org.academy.api.client.gui.text.glyph.bitmap.BitmapAtlas
import java.io.IOException
import java.nio.file.Files
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
        // 1.21.1: AtlasPage 在 CPU 侧持有 NativeImage (位图灰度已在 upload 时放大为 RGBA8),
        // 直接写 PNG, 无需 26.2 的 RenderDevice/GpuBuffer 回读管线.
        val buffer = page.pixelBuffer() ?: run {
            logger.warn("Bitmap atlas page ({}) has not been uploaded yet; skip dump", pageIndex)
            return file
        }
        try {
            Files.createDirectories(outputDir)
            buffer.writeToFile(file)
            logger.info("Dumped bitmap atlas page to {}", file)
        } catch (e: IOException) {
            logger.error("Unable to write bitmap atlas page {}", pageIndex, e)
        }
        return file
    }
}
