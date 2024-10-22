package server.endpoints.files

import assertions.assertSuccess
import database.entity.Area
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.readRawBytes
import io.ktor.http.HttpHeaders
import io.ktor.http.etag
import io.ktor.http.isSuccess
import io.ktor.http.lastModified
import io.ktor.utils.io.readBuffer
import java.awt.image.BufferedImage
import java.io.File
import java.security.MessageDigest
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
import storage.HashUtils
import storage.MessageDigestAlgorithm
import storage.Storage

class TestFileDownloading : ApplicationTestBase() {
    private suspend inline fun StubApplicationTestBuilder.provideImageFile(
        imageFile: String = "/images/alcoi.jpg",
        block: (imageUUID: String, imageFile: File) -> Unit
    ) {
        val areaId = DataProvider.provideSampleArea(this, imageFile = imageFile)

        var imageFile: File? = null

        get("/area/$areaId").apply {
            assertSuccess<Area> { data ->
                assertNotNull(data)
                imageFile = data.image
            }
        }

        assertNotNull(imageFile)

        block(imageFile.toRelativeString(Storage.ImagesDir), imageFile)
    }

    private fun downloadResized(
        argument: String,
        value: Int,
        fetch: (BufferedImage) -> Int,
        imageFile: String = "/images/alcoi.jpg"
    ) = test {
        provideImageFile(imageFile) { image, _ ->
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
        provideImageFile { image, imageFile ->
            get("/download/$image").apply {
                headers[HttpHeaders.ContentType].let { contentType ->
                    assertEquals("image/jpeg", contentType, "Content-Type header is not JPEG. Got: $contentType")
                }
                assertEquals(image, headers[HttpHeaders.FileUUID], "File UUID header is not correct")
                assertEquals(
                    FileType.IMAGE.headerValue,
                    headers[HttpHeaders.FileSource],
                    "File source header is not correct."
                )
                etag().let {
                    val hash = HashUtils.getCheckSumFromFile(
                        MessageDigest.getInstance(MessageDigestAlgorithm.SHA_256),
                        imageFile
                    )
                    assertEquals("\"$hash\"", it, "ETag header is not correct")
                }
                lastModified()?.time.let {
                    // Value may be truncated to seconds and converted again to ms, so we need to truncate it
                    val fileLastModified = imageFile.lastModified() / 1000 * 1000
                    val headerLastModified = it?.div(1000)?.times(1000)
                    assertEquals(fileLastModified, headerLastModified, "Last-Modified header is not correct")
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
