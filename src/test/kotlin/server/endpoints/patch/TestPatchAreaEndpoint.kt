package server.endpoints.patch

import ServerDatabase
import assertions.assertSuccess
import database.entity.Area
import database.entity.info.LastUpdate
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.header
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import server.DataProvider
import server.base.ApplicationTestBase

class TestPatchAreaEndpoint : ApplicationTestBase() {
    @Test
    fun `test patching Area - update display name`() = test {
        val areaId = DataProvider.provideSampleArea()
        assertNotNull(areaId)

        val lastUpdate = ServerDatabase.instance.query { LastUpdate.get() }

        client.submitFormWithBinaryData(
            url = "/area/$areaId",
            formData = formData {
                append("displayName", "New Display Name")
            }
        ) {
            header(HttpHeaders.Authorization, "Bearer $AUTH_TOKEN")
        }.apply {
            assertSuccess()
        }

        ServerDatabase.instance.query { assertNotEquals(LastUpdate.get(), lastUpdate) }

        ServerDatabase.instance.query {
            val area = Area[areaId]
            assertNotNull(area)
            assertEquals("New Display Name", area.displayName)
        }
    }

    @Test
    fun `test patching Area - update web url`() = test {
        val areaId = DataProvider.provideSampleArea()
        assertNotNull(areaId)

        client.submitFormWithBinaryData(
            url = "/area/$areaId",
            formData = formData {
                append("webUrl", "https://example.com/new")
            }
        ) {
            header(HttpHeaders.Authorization, "Bearer $AUTH_TOKEN")
        }.apply {
            assertSuccess()
        }

        ServerDatabase.instance.query {
            val area = Area[areaId]
            assertNotNull(area)
            assertEquals("https://example.com/new", area.webUrl.toString())
        }
    }

    @Test
    fun `test patching Area - update image`() = test {
        val areaId = DataProvider.provideSampleArea()
        assertNotNull(areaId)

        val image = this::class.java.getResourceAsStream("/images/cocentaina.jpg")!!.use {
            it.readBytes()
        }

        client.submitFormWithBinaryData(
            url = "/area/$areaId",
            formData = formData {
                append("image", image, Headers.build {
                    append(HttpHeaders.ContentType, "image/jpeg")
                    append(HttpHeaders.ContentDisposition, "filename=area.jpg")
                })
            }
        ) {
            header(HttpHeaders.Authorization, "Bearer $AUTH_TOKEN")
        }.apply {
            assertSuccess()
        }

        ServerDatabase.instance.query {
            val area = Area[areaId]
            assertNotNull(area)

            val imageFile = area.image
            assertTrue(imageFile.exists())
        }
    }
}
