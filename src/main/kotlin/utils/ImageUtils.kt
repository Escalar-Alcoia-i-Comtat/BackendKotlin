package utils

import java.awt.Color
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.EOFException
import java.io.File
import java.io.OutputStream
import java.nio.file.Files
import javax.imageio.IIOException
import javax.imageio.ImageIO
import javax.imageio.ImageReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ImageUtils {
    private const val TRUNCATED_BYTE_A: Byte = (0xff).toByte()
    private const val TRUNCATED_BYTE_B: Byte = (0xd9).toByte()

    val supportedExtensions = listOf("png", "jpeg", "jpg", "webp")

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

    suspend fun scale(imageFile: File, width: Int?, height: Int?, outputStream: OutputStream, format: String = "webp") {
        withContext(Dispatchers.IO) {
            check(width != null || height != null) { "Must provide either width, height or both, but not none." }

            val img = ImageIO.read(imageFile)

            var imageWidth = if (width == null || width <= 0) {
                (height!! * img.width) / img.height
            } else {
                width
            }
            var imageHeight = if (height == null || height <= 0) {
                (width!! * img.height) / img.width
            } else {
                height
            }

            if (imageWidth > img.width || imageHeight > img.height) {
                // Do not allow oversizing
                imageWidth = img.width
                imageHeight = img.height
            }

            val scaledImage = img.getScaledInstance(imageWidth, imageHeight, Image.SCALE_SMOOTH)
            val imageBuff = BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB)
            imageBuff.graphics.drawImage(scaledImage, 0, 0, Color(0, 0, 0), null)

            ImageIO.write(imageBuff, format, outputStream)
        }
    }
}
