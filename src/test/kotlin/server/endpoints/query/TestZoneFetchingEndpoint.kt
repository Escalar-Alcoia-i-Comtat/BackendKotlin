package server.endpoints.query

import assertions.assertFailure
import assertions.assertIsUUID
import assertions.assertSuccess
import database.entity.Zone
import io.ktor.http.HttpStatusCode
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import server.DataProvider
import server.base.ApplicationTestBase
import server.error.Errors
import server.response.files.RequestFileResponseData
import storage.Storage

class TestZoneFetchingEndpoint: ApplicationTestBase() {
    @Test
    fun `test getting zone`() = test {
        val areaId = DataProvider.provideSampleArea()
        assertNotNull(areaId)

        val zoneId = DataProvider.provideSampleZone(areaId)
        assertNotNull(zoneId)

        var image: String? = null
        var kmz: String? = null

        get("/zone/$zoneId").apply {
            assertSuccess<Zone> { data ->
                assertNotNull(data)

                assertEquals(zoneId, data.id.value)
                assertEquals(areaId, data.area.id.value)
                assertEquals(DataProvider.SampleZone.displayName, data.displayName)
                assertEquals(DataProvider.SampleZone.webUrl, data.webUrl.toString())
                assertTrue(data.timestamp < Instant.now())

                image = data.image.toRelativeString(Storage.ImagesDir)
                kmz = data.kmz.toRelativeString(Storage.TracksDir)
            }
        }

        assertNotNull(image)
        assertIsUUID(image!!)
        assertNotNull(kmz)
        assertIsUUID(kmz!!)

        get("/file/$image").apply {
            assertSuccess<RequestFileResponseData>()
        }

        get("/file/$kmz").apply {
            assertSuccess<RequestFileResponseData>()
        }
    }

    @Test
    fun `test getting zone - doesn't exist`() = test {
        get("/zone/123").apply {
            assertFailure(Errors.ObjectNotFound)
        }
    }

    @Test
    fun `test getting zone - id NaN`() = test {
        get("/zone/abc").apply {
            assertEquals(HttpStatusCode.BadRequest, status)
        }
    }
}
