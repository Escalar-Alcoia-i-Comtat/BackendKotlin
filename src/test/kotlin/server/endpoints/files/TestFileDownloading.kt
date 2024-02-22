package server.endpoints.files

import assertions.assertSuccess
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.readBytes
import io.ktor.http.isSuccess
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.util.cio.writeChannel
import io.ktor.utils.io.copyAndClose
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import server.DataProvider
import server.base.ApplicationTestBase

class TestFileDownloading : ApplicationTestBase() {
    private suspend inline fun ApplicationTestBuilder.provideImageFile(
        imageFile: String = "/images/alcoi.jpg",
        block: (imageUUID: String) -> Unit
    ) {
        val areaId = DataProvider.provideSampleArea(imageFile = imageFile)

        var image: String? = null

        get("/area/$areaId").apply {
            assertSuccess { data ->
                assertNotNull(data)
                image = data.getString("image")
            }
        }

        assertNotNull(image)

        block(image!!)
    }

    @Test
    fun `test downloading files`() = test {
        provideImageFile { image ->
            get("/download/$image").apply {
                headers["Content-Type"].let { contentType ->
                    assertEquals(
                        "image/jpeg",
                        contentType,
                        "Content-Type header is not JPEG. Got: $contentType"
                    )
                }
                readBytes()
            }
        }
    }

    @Test
    fun `test downloading resized files - width`() = test {
        provideImageFile { image ->
            val tempFile = File.createTempFile("eaic", null)
            val response = get("/download/$image?width=200")
            assertTrue(
                response.status.isSuccess(),
                "Got a non-successful response from server. Status: ${response.status}"
            )

            val channel = response.bodyAsChannel()
            channel.copyAndClose(tempFile.writeChannel())

            try {
                val img: BufferedImage? = ImageIO.read(tempFile)
                assertEquals(200, img?.width)
            } finally {
                tempFile.delete()
            }
        }
    }

    @Test
    fun `test downloading resized files - height`() = test {
        provideImageFile { image ->
            val tempFile = File.createTempFile("eaic", null)
            val response = get("/download/$image?height=200")
            assertTrue(
                response.status.isSuccess(),
                "Got a non-successful response from server. Status: ${response.status}"
            )

            val channel = response.bodyAsChannel()
            channel.copyAndClose(tempFile.writeChannel())

            try {
                val img: BufferedImage? = ImageIO.read(tempFile)
                assertNotNull(img)
                assertEquals(200, img.height)
            } finally {
                tempFile.delete()
            }
        }
    }

    @Test
    fun `test downloading resized files (webp)`() = test {
        provideImageFile("/images/alcoi.webp") { image ->
            val tempFile = File.createTempFile("eaic", null)
            val response = get("/download/$image?height=200")
            assertTrue(
                response.status.isSuccess(),
                "Got a non-successful response from server. Status: ${response.status}"
            )

            val channel = response.bodyAsChannel()
            channel.copyAndClose(tempFile.writeChannel())

            try {
                val img: BufferedImage? = ImageIO.read(tempFile)
                assertNotNull(img)
                assertEquals(200, img.height)
            } finally {
                tempFile.delete()
            }
        }
    }
}
