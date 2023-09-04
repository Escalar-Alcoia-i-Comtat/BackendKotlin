package com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.query

import com.arnyminerz.escalaralcoiaicomtat.backend.assertions.assertFailure
import com.arnyminerz.escalaralcoiaicomtat.backend.assertions.assertSuccess
import com.arnyminerz.escalaralcoiaicomtat.backend.server.DataProvider
import com.arnyminerz.escalaralcoiaicomtat.backend.server.base.ApplicationTestBase
import com.arnyminerz.escalaralcoiaicomtat.backend.server.error.Errors
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.getLongOrNull
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.getStringOrNull
import io.ktor.http.HttpStatusCode
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

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
        assertNotNull(kmz)

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
