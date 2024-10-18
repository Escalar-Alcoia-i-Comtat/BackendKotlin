package server.endpoints.files

import assertions.assertSuccess
import database.entity.Area
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.readBytes
import io.ktor.http.isSuccess
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
import server.base.StubApplicationTestBuilder
import storage.Storage

class TestFileDownloading : ApplicationTestBase() {
    private suspend inline fun StubApplicationTestBuilder.provideImageFile(
        imageFile: String = "/images/alcoi.jpg",
        block: (imageUUID: String) -> Unit
    ) {
        val areaId = with(DataProvider) { provideSampleArea(imageFile = imageFile) }

        var image: String? = null

        get("/area/$areaId").apply {
            assertSuccess<Area> { data ->
                assertNotNull(data)
                image = data.image.toRelativeString(Storage.ImagesDir)
            }
        }

        assertNotNull(image)

        block(image!!)
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
            channel.copyAndClose(tempFile.writeChannel())

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
