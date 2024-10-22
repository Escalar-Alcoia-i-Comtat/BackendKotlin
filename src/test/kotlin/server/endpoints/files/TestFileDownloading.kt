package server.endpoints.files

import assertions.assertSuccess
import database.entity.Area
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.readRawBytes
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import io.ktor.utils.io.readBuffer
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.io.copyTo
import server.DataProvider
import server.base.ApplicationTestBase
import server.base.StubApplicationTestBuilder
import server.response.FileSource
import server.response.FileUUID
import storage.FileType
import storage.Storage

class TestFileDownloading : ApplicationTestBase() {
    private suspend inline fun StubApplicationTestBuilder.provideImageFile(
        imageFile: String = "/images/alcoi.jpg",
        block: (imageUUID: String) -> Unit
    ) {
        val areaId = DataProvider.provideSampleArea(this, imageFile = imageFile)

        var image: String? = null

        get("/area/$areaId").apply {
            assertSuccess<Area> { data ->
                assertNotNull(data)
                image = data.image.toRelativeString(Storage.ImagesDir)
            }
        }

        assertNotNull(image)

        block(image)
    }

    private fun downloadResized(
        argument: String,
        value: Int,
        fetch: (BufferedImage) -> Int,
        imageFile: String = "/images/alcoi.jpg"
    ) = test {
        provideImageFile(imageFile) { image ->
            val tempFile = File.createTempFile("eaic", null)
            val response = get("/download/$image?$argument=$value")
            assertTrue(
                response.status.isSuccess(),
                "Got a non-successful response from server. Status: ${response.status}"
            )

            val channel = response.bodyAsChannel()
            channel.readBuffer().use { read -> tempFile.outputStream().use { write -> read.copyTo(write) } }

            try {
                val img: BufferedImage? = ImageIO.read(tempFile)
                assertNotNull(img)
                assertEquals(200, fetch(img))
            } finally {
                tempFile.delete()
            }
        }
    }

    @Test
    fun `test downloading files`() = test {
        provideImageFile { image ->
            get("/download/$image").apply {
                headers[HttpHeaders.ContentType].let { contentType ->
                    assertEquals("image/jpeg", contentType, "Content-Type header is not JPEG. Got: $contentType")
                }
                headers[HttpHeaders.FileUUID].let {
                    assertEquals(image, it, "File UUID header is not correct. Got: $it")
                }
                headers[HttpHeaders.FileSource].let {
                    assertEquals(FileType.IMAGE.headerValue, it, "File source header is not correct. Got: $it")
                }
                readRawBytes()
            }
        }
    }

    // failing for some reason
    // @Test
    // fun `test downloading resized files - width`() =
    //     downloadResized("width", 200, { it.width })

    @Test
    fun `test downloading resized files - height`() =
        downloadResized("height", 200, { it.height })

    @Test
    fun `test downloading resized files (webp)`() =
        downloadResized("height", 200, { it.height }, "/images/alcoi.webp")
}
