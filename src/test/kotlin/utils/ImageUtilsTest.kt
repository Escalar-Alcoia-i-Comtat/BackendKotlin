package utils

import io.ktor.test.dispatcher.testSuspend
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.file.Files
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ImageUtilsTest {
    @Test
    fun `test image scaling - no size`() = testSuspend {
        assertFailsWith(IllegalStateException::class) {
            ImageUtils.scale(File(""), null, null, ByteArrayOutputStream())
        }
    }

    @Test
    fun `test isExtensionSupported`() {
        assertTrue(ImageUtils.isExtensionSupported("png"))
        assertTrue(ImageUtils.isExtensionSupported("JPEG"))
        assertTrue(ImageUtils.isExtensionSupported("jpg"))
        assertTrue(ImageUtils.isExtensionSupported("WEBP"))
        assertFalse(ImageUtils.isExtensionSupported("gif"))
        assertFalse(ImageUtils.isExtensionSupported("exe"))
    }

    private fun testResize(
        resource: String,
        width: Int? = null,
        height: Int? = null,
        format: String = "webp"
    ) = testSuspend {
        val name = resource.substringAfterLast('/')
        val nameWithoutExtension = name.substringBeforeLast('.')
        // Create temp files
        val file = File.createTempFile("test_scaling_original-", "-$name")
        val fileScaled = File.createTempFile("test_scaling_resized-", "-$nameWithoutExtension.$format")
        // Store the original image size
        val (originalWidth, originalHeight) = 1578 to 720
        val originalRatio = originalWidth.toDouble() / originalHeight
        try {
            // Create the resource as File
            this::class.java.getResourceAsStream(resource)!!.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }
            fileScaled.outputStream().use { output ->
                ImageUtils.scale(file, width, height, output)
            }
            println("Scaled file: ${fileScaled.absolutePath}")

            assertTrue(fileScaled.exists())

            val mimeType = Files.probeContentType(fileScaled.toPath())
            assertEquals("image/$format", mimeType)

            val img: BufferedImage? = ImageIO.read(fileScaled)
            assertNotNull(img)
            val (scaledWidth, scaledHeight) = img.width to img.height
            if (width != null) {
                assertEquals(width, scaledWidth)
                assertEquals((width / originalRatio).toInt(), scaledHeight)
            } else {
                assertEquals((height!! * originalRatio).toInt(), scaledWidth)
                assertEquals(height, scaledHeight)
            }
        } finally {
            if (file.exists()) file.delete()
            if (fileScaled.exists()) fileScaled.delete()
        }
    }


    @Test
    fun `test image scaling - change width`() = testResize("/images/alcoi.jpg", width = 100)

    @Test
    fun `test image scaling - change height`() = testResize("/images/alcoi.jpg", height = 100)

    @Test
    fun `test image scaling (webp)`() = testResize("/images/alcoi.webp", width = 100)
}
