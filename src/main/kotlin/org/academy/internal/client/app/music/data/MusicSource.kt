package org.academy.internal.client.app.music.data

import net.minecraft.client.Minecraft
import net.minecraft.resources.ResourceLocation
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Path
import java.util.function.Supplier

data class MusicSource(val path: Any) {
    val data: ByteBuffer
        get() {
            val bytes: ByteArray
            when (path) {
                is ResourceLocation -> Minecraft.getInstance().resourceManager.open(path).use {
                    bytes = it.readAllBytes()
                }

                is String -> bytes = Files.readAllBytes(Path.of(path))

                is Supplier<*> -> {
                    val supplied = path.get()
                    if (supplied !is ByteBuffer) throw IOException("Music source did not provide a ByteBuffer")
                    return supplied.duplicate()
                }

                else -> throw IOException("Unsupported media source type: " + path.javaClass.getName())
            }

            val buffer = ByteBuffer.allocateDirect(bytes.size)
            buffer.put(bytes).flip()
            return buffer
        }

    companion object {
        fun fromIdentifier(location: ResourceLocation): MusicSource {
            return MusicSource(location)
        }

        fun fromAbsolutePath(absolutePath: String): MusicSource {
            return MusicSource(absolutePath)
        }

        fun fromSupplier(supplier: Supplier<ByteBuffer>): MusicSource {
            return MusicSource(supplier)
        }
    }
}
