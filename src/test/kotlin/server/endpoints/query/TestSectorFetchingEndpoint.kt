package server.endpoints.query

import ServerDatabase
import assertions.assertFailure
import assertions.assertIsUUID
import assertions.assertSuccess
import database.entity.Sector
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
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

class TestSectorFetchingEndpoint : ApplicationTestBase() {
    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun `test getting sector`() = test {
        val areaId = DataProvider.provideSampleArea(this)
        assertNotNull(areaId)

        val zoneId = DataProvider.provideSampleZone(this, areaId)
        assertNotNull(zoneId)

        val sectorId = DataProvider.provideSampleSector(this, zoneId)
        assertNotNull(sectorId)

        var image: String? = null
        var gpx: String? = null

        get("/sector/$sectorId").apply {
            assertSuccess<Sector> { data ->
                assertNotNull(data)

                assertEquals(sectorId, data.id.value)
                assertEquals(zoneId, data.zone.id.value)
                assertEquals(DataProvider.SampleSector.displayName, data.displayName)
                assertEquals(DataProvider.SampleSector.point, data.point)
                assertEquals(DataProvider.SampleSector.kidsApt, data.kidsApt)
                assertEquals(DataProvider.SampleSector.sunTime, data.sunTime)
                assertEquals(DataProvider.SampleSector.walkingTime, data.walkingTime)
                assertTrue(data.timestamp < Instant.now())

                image = data.image.toRelativeString(Storage.ImagesDir).substringBeforeLast('.')
                gpx = data.gpx?.toRelativeString(Storage.TracksDir)?.substringBeforeLast('.')
            }

            val sector = ServerDatabase { Sector[sectorId] }
            val hashCode = ServerDatabase { sector.hashCode().toHexString() }
            assertEquals("Sector", headers[HttpHeaders.ResourceType])
            assertEquals(sectorId, headers[HttpHeaders.ResourceId]?.toIntOrNull())
            assertEquals(sector.timestamp.epochSecond, lastModified()?.toInstant()?.epochSecond)
            assertEquals(hashCode, etag()?.trim('"'))
        }

        assertNotNull(image)
        assertIsUUID(image)

        assertNotNull(gpx)
        assertIsUUID(gpx!!)

        get("/file/$image").apply {
            assertSuccess<RequestFileResponseData>()
        }

        get("/file/$gpx").apply {
            assertSuccess<RequestFileResponseData>()
        }
    }

    @Test
    fun `test getting sector - doesn't exist`() = test {
        get("/sector/123").apply {
            assertFailure(Errors.ObjectNotFound)
        }
    }

    @Test
    fun `test getting sector - id NaN`() = test {
        get("/sector/abc").apply {
            assertEquals(HttpStatusCode.BadRequest, status)
        }
    }
}
