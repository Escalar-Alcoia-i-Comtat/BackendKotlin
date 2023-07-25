package com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.query

import com.arnyminerz.escalaralcoiaicomtat.backend.assertions.assertFailure
import com.arnyminerz.escalaralcoiaicomtat.backend.assertions.assertSuccess
import com.arnyminerz.escalaralcoiaicomtat.backend.data.LatLng
import com.arnyminerz.escalaralcoiaicomtat.backend.database.entity.Sector
import com.arnyminerz.escalaralcoiaicomtat.backend.server.base.ApplicationTestBase
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.DataProvider
import com.arnyminerz.escalaralcoiaicomtat.backend.server.error.Errors
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.getBooleanOrNull
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.getEnumOrNull
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.getLongOrNull
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.getStringOrNull
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.getUIntOrNull
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

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

        client.get("/sector/$sectorId").apply {
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

        client.get("/file/$image").apply {
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
        client.get("/sector/123").apply {
            assertFailure(Errors.ObjectNotFound)
        }
    }

    @Test
    fun `test getting sector - id NaN`() = test {
        client.get("/sector/abc").apply {
            assertEquals(HttpStatusCode.BadRequest, status)
        }
    }
}
