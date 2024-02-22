package server.endpoints.query

import assertions.assertFailure
import assertions.assertIsUUID
import assertions.assertSuccess
import io.ktor.http.HttpStatusCode
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import server.DataProvider
import server.base.ApplicationTestBase
import server.error.Errors
import utils.getLongOrNull
import utils.getStringOrNull

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
            assertSuccess { data ->
                assertNotNull(data)

                assertEquals(zoneId, data.getInt("id"))
                assertEquals(areaId, data.getInt("area_id"))
                assertEquals(DataProvider.SampleZone.displayName, data.getString("display_name"))
                assertEquals(DataProvider.SampleZone.webUrl, data.getString("web_url"))
                assertTrue(data.getLong("timestamp") < Instant.now().toEpochMilli())

                image = data.getString("image")
                kmz = data.getString("kmz")
            }
        }

        assertNotNull(image)
        assertIsUUID(image!!)
        assertNotNull(kmz)
        assertIsUUID(kmz!!)

        get("/file/$image").apply {
            assertSuccess { data ->
                assertNotNull(data?.getStringOrNull("download"))
                assertNotNull(data?.getStringOrNull("filename"))
                assertNotNull(data?.getStringOrNull("hash"))
                assertNotNull(data?.getLongOrNull("size"))
            }
        }

        get("/file/$kmz").apply {
            assertSuccess { data ->
                assertNotNull(data?.getStringOrNull("download"))
                assertNotNull(data?.getStringOrNull("filename"))
                assertNotNull(data?.getStringOrNull("hash"))
                assertNotNull(data?.getLongOrNull("size"))
            }
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
