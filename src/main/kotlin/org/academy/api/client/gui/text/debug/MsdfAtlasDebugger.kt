package org.academy.api.client.gui.text.debug

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.Minecraft
import org.academy.AcademyCraft
import org.academy.api.client.gui.text.glyph.msdf.MsdfAtlas
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
            // C-stun: 1.21.1 的 TextureUtil.writeAsPNG 不接收 GpuTexture, MSDF 图集调试导出暂不执行.
            logger.warn("MSDF atlas debug dump is disabled in 1.21.1 port (page {})", pageIndex)
        }
        return written
    }

    fun dumpAtlas(atlas: MsdfAtlas, fileNamePrefix: String) {
        val gameDirectory = Minecraft.getInstance().gameDirectory.toPath().toAbsolutePath()
        dumpAtlas(atlas, gameDirectory, fileNamePrefix)
    }
}
