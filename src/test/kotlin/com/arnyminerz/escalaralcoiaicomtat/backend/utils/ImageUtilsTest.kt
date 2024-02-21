package com.arnyminerz.escalaralcoiaicomtat.backend.utils

import io.ktor.test.dispatcher.testSuspend
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ImageUtilsTest {
    @Test
    fun `test image scaling - no size`() = testSuspend {
        assertFailsWith(IllegalStateException::class) {
            ImageUtils.scale(File(""), null, null, ByteArrayOutputStream())
        }
    }

    private fun testResize(width: Int? = null, height: Int? = null) = testSuspend {
        // Create temp files
        val file = File.createTempFile("test_scaling_original-", "-alcoi.jpg")
        val fileScaled = File.createTempFile("test_scaling_resized-", "-alcoi.avif")
        // Store the original image size
        val (originalWidth, originalHeight) = 1578 to 720
        val originalRatio = originalWidth.toDouble() / originalHeight
        try {
            // Create the resource as File
            this::class.java.getResourceAsStream("/images/alcoi.jpg")!!.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }
            fileScaled.outputStream().use { output ->
                ImageUtils.scale(file, width, height, output)
            }
            println("Scaled file: ${fileScaled.absolutePath}")
            assertTrue(fileScaled.exists())
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
    fun `test image scaling - change width`() = testResize(width = 100)

    @Test
    fun `test image scaling - change height`() = testResize(height = 100)
}
