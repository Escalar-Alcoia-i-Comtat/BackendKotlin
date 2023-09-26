package com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.files

import com.arnyminerz.escalaralcoiaicomtat.backend.assertions.assertSuccess
import com.arnyminerz.escalaralcoiaicomtat.backend.server.DataProvider
import com.arnyminerz.escalaralcoiaicomtat.backend.server.base.ApplicationTestBase
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

class TestFileDownloading : ApplicationTestBase() {
    private suspend inline fun ApplicationTestBuilder.provideImageFile(block: (imageUUID: String) -> Unit) {
        val areaId = DataProvider.provideSampleArea()

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
}
