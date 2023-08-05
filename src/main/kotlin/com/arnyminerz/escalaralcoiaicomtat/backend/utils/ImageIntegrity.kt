package com.arnyminerz.escalaralcoiaicomtat.backend.utils

import java.awt.image.BufferedImage
import java.io.EOFException
import java.io.File
import java.nio.file.Files
import javax.imageio.IIOException
import javax.imageio.ImageIO
import javax.imageio.ImageReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ImageIntegrity {
    private const val TRUNCATED_BYTE_A: Byte = (0xff).toByte()
    private const val TRUNCATED_BYTE_B: Byte = (0xd9).toByte()

    /**
     * Verifies the integrity of an image file.
     *
     * @param file The image file to verify.
     * @return An ImageAnalysisResult object containing the analysis result.
     */
    suspend fun verifyImageIntegrity(file: File): ImageAnalysisResult = withContext(Dispatchers.IO) {
        val result = ImageAnalysisResult()

        try {
            Files.newInputStream(file.toPath()).use { digestInputStream ->
                val imageInputStream = ImageIO.createImageInputStream(digestInputStream)
                val imageReaders = ImageIO.getImageReaders(imageInputStream)
                if (!imageReaders.hasNext()) {
                    result.image = false
                    return@withContext result
                }

                val imageReader: ImageReader = imageReaders.next()
                imageReader.input = imageInputStream

                val image: BufferedImage = imageReader.read(0) ?: return@withContext result
                image.flush()

                if (imageReader.formatName == "JPEG") {
                    imageInputStream.seek(imageInputStream.streamPosition - 2)
                    val lastTwoBytes = ByteArray(2)
                    imageInputStream.read(lastTwoBytes)

                    result.truncated = lastTwoBytes[0] != TRUNCATED_BYTE_A || lastTwoBytes[1] != TRUNCATED_BYTE_B
                }
                result.image = true
            }
        } catch (_: IndexOutOfBoundsException) {
            result.truncated = true
        } catch (e: IIOException) {
            if (e.cause is EOFException) {
                result.truncated = true
            }
        }

        result
    }

    class ImageAnalysisResult {
        var image: Boolean? = null
        var truncated: Boolean? = null
    }
}
