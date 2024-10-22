package server.endpoints.query

import ServerDatabase
import assertions.assertFailure
import assertions.assertIsUUID
import assertions.assertSuccess
import database.entity.Area
import io.ktor.http.HttpHeaders
import io.ktor.http.etag
import io.ktor.http.lastModified
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import server.DataProvider
import server.base.ApplicationTestBase
import server.error.Errors
import server.response.ResourceId
import server.response.ResourceType
import server.response.files.RequestFileResponseData
import storage.Storage

class TestAreaFetchingEndpoint : ApplicationTestBase() {
    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun `test getting area`() = test {
        val areaId = DataProvider.provideSampleArea(this)
        assertNotNull(areaId)

        var image: String? = null

        get("/area/$areaId").apply {
            assertSuccess<Area> { data ->
                assertNotNull(data)

                assertEquals(areaId, data.id.value)
                assertEquals(DataProvider.SampleArea.displayName, data.displayName)
                assertEquals(DataProvider.SampleArea.webUrl, data.webUrl.toString())
                assertTrue(data.timestamp < Instant.now())

                image = data.image.toRelativeString(Storage.ImagesDir).substringBeforeLast('.')
            }

            val area = ServerDatabase { Area[areaId] }
            val hashCode = ServerDatabase { area.hashCode().toHexString() }
            assertEquals("Area", headers[HttpHeaders.ResourceType])
            assertEquals(areaId, headers[HttpHeaders.ResourceId]?.toIntOrNull())
            assertEquals(area.timestamp.epochSecond, lastModified()?.toInstant()?.epochSecond)
            assertEquals(hashCode, etag()?.trim('"'))
        }

        assertNotNull(image)
        assertIsUUID(image)

        get("/file/$image").apply {
            assertSuccess<RequestFileResponseData>()
        }
    }

    @Test
    fun `test getting area - doesn't exist`() = test {
        get("/area/123").apply {
            assertFailure(Errors.ObjectNotFound)
        }
    }
}
