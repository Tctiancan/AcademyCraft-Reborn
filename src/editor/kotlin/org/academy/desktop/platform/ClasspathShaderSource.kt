package org.academy.desktop.platform

import com.mojang.blaze3d.preprocessor.GlslPreprocessor
import org.com.mojang.blaze3d.shaders.ShaderType
import net.minecraft.resources.ResourceLocation
import java.nio.file.Files
import java.nio.file.Path

/**
 * Blaze3D [org.com.mojang.blaze3d.shaders.ShaderSource] that resolves GLSL and expands `#moj_import`
 * with the vanilla [GlslPreprocessor]. Lookup order:
 *  1. [sourceDir] 下的 `assets/<ns>/shaders/<path><ext>`（编辑器源目录 —— **热重载**：改源文件即生效）
 *  2. classpath `assets/<ns>/shaders/<path><ext>`
 *  3. vanilla jar（被 FML 过滤的源 shader 从 vanilla 直接读取）
 */
object ClasspathShaderSource {
    /** 指向项目 `src/main/resources`；非空则优先读源文件，配合文件监听实现着色器热重载。 */
    @Volatile
    var sourceDir: Path? = null

    fun read(id: ResourceLocation, type: ShaderType): String? {
        val extension = if (type == ShaderType.VERTEX) ".vsh" else ".fsh"
        val raw = readResource(ResourceLocation.fromNamespaceAndPath(id.namespace, "shaders/${id.path}$extension"))
            ?: return null

        val preprocessor = object : GlslPreprocessor() {
            override fun applyImport(isRelative: Boolean, path: String) = if (isRelative) {
                readResource(ResourceLocation.fromNamespaceAndPath(id.namespace, "shaders/include/$path"))
            } else {
                readResource(ResourceLocation.parse(path).withPrefix("shaders/include/"))
            }
        }
        return preprocessor.process(raw).joinToString("")
    }

    private fun readResource(location: ResourceLocation): String? {
        readFromSourceDir(location)?.let { return it }
        readFromClasspath(location)?.let { return it }
        return readFromVanillaJar(location)
    }

    private fun readFromSourceDir(location: ResourceLocation): String? {
        val root = sourceDir ?: return null
        val file = root.resolve("assets").resolve(location.namespace).resolve(location.path)
        return if (Files.isRegularFile(file)) Files.readString(file) else null
    }

    private fun readFromClasspath(location: ResourceLocation): String? {
        return ClasspathShaderSource.javaClass
            .getResourceAsStream("/assets/${location.namespace}/${location.path}")
            ?.use { stream -> stream.bufferedReader().readText() }
    }

    private fun readFromVanillaJar(location: ResourceLocation): String? {
        return try {
            val codeSource = net.minecraft.SharedConstants::class.java.protectionDomain.codeSource ?: return null
            val locationUrl = codeSource.location
            if (locationUrl.protocol != "file") return null
            val jarFile = java.io.File(locationUrl.toURI().path)
            if (!jarFile.isFile) return null
            java.util.zip.ZipFile(jarFile).use { zip ->
                val entry = zip.getEntry("assets/${location.namespace}/${location.path}") ?: return null
                zip.getInputStream(entry).use { stream -> stream.bufferedReader().readText() }
            }
        } catch (e: Exception) {
            null
        }
    }
}
