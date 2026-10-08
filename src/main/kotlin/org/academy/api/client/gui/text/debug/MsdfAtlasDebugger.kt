package org.academy.api.client.gui.text.debug

import com.mojang.blaze3d.platform.NativeImage
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.Minecraft
import org.academy.AcademyCraft
import org.academy.api.client.gui.text.glyph.msdf.MsdfAtlas
import org.academy.api.client.gui.text.atlas.AtlasPage
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

object MsdfAtlasDebugger {
    private val logger = AcademyCraft.getLogger()

    fun dumpAtlas(atlas: MsdfAtlas, outputDir: Path, fileNamePrefix: String): List<Path> {
        RenderSystem.assertOnRenderThread()
        val pages = atlas.getPages()
        val written = ArrayList<Path>(pages.size)
        try {
            Files.createDirectories(outputDir)
        } catch (e: IOException) {
            logger.error("Failed to create directory {}", outputDir, e)
            return written
        }

        for (pageIndex in pages.indices) {
            val prefix = fileNamePrefix + pageIndex
            dumpPage(pages[pageIndex], outputDir, prefix)
            written.add(outputDir.resolve(prefix + ".png"))
        }
        return written
    }

    private fun dumpPage(page: AtlasPage, outputDir: Path, prefix: String) {
        // 1.21.1: AtlasPage 在 CPU 侧持有 NativeImage (MSDF RGBA8), 直接写 PNG, 无需 26.2 的 GPU 回读.
        val buffer = page.pixelBuffer() ?: run {
            logger.warn("Atlas page has not been uploaded yet; skip dump {}", prefix)
            return
        }
        val file = outputDir.resolve(prefix + ".png")
        try {
            buffer.writeToFile(file)
            logger.info("Dumped MSDF atlas page to {}", file)
        } catch (e: IOException) {
            logger.error("Failed to dump MSDF atlas page {}", prefix, e)
        }
    }

    fun dumpAtlas(atlas: MsdfAtlas, fileNamePrefix: String) {
        val gameDirectory = Minecraft.getInstance().gameDirectory.toPath().toAbsolutePath()
        dumpAtlas(atlas, gameDirectory, fileNamePrefix)
    }
}
