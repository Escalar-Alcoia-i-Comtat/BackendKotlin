package server.endpoints.query

import assertions.assertFailure
import assertions.assertIsUUID
import assertions.assertSuccess
import data.LatLng
import database.entity.Sector
import io.ktor.http.HttpStatusCode
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import server.DataProvider
import server.base.ApplicationTestBase
import server.error.Errors
import utils.getBooleanOrNull
import utils.getEnumOrNull
import utils.getLongOrNull
import utils.getStringOrNull
import utils.getUIntOrNull

class TestSectorFetchingEndpoint : ApplicationTestBase() {
    @Test
    fun `test getting sector`() = test {
        val areaId = DataProvider.provideSampleArea()
        assertNotNull(areaId)

        val zoneId = DataProvider.provideSampleZone(areaId)
        assertNotNull(zoneId)

        val sectorId = DataProvider.provideSampleSector(zoneId)
        assertNotNull(sectorId)

        var image: String? = null

        get("/sector/$sectorId").apply {
            assertSuccess { data ->
                assertNotNull(data)

                println(data.toString(2))

                assertEquals(sectorId, data.getInt("id"))
                assertEquals(zoneId, data.getInt("zone_id"))
                assertEquals(DataProvider.SampleSector.displayName, data.getString("display_name"))
                assertEquals(
                    DataProvider.SampleSector.point,
                    data.getJSONObject("point").let { LatLng.fromJson(it) }
                )
                assertEquals(DataProvider.SampleSector.kidsApt, data.getBooleanOrNull("kids_apt"))
                assertEquals(
                    DataProvider.SampleSector.sunTime,
                    data.getEnumOrNull(Sector.SunTime::class, "sun_time")
                )
                assertEquals(DataProvider.SampleSector.walkingTime, data.getUIntOrNull("walking_time"))
                assertTrue(data.getLong("timestamp") < Instant.now().toEpochMilli())

                image = data.getString("image")
            }
        }

        assertNotNull(image)
        assertIsUUID(image!!)

        get("/file/$image").apply {
            assertSuccess { data ->
                assertNotNull(data?.getStringOrNull("download"))
                assertNotNull(data?.getStringOrNull("filename"))
                assertNotNull(data?.getStringOrNull("hash"))
                assertNotNull(data?.getLongOrNull("size"))
            }
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
