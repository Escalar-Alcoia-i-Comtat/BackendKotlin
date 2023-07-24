package com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.query

import com.arnyminerz.escalaralcoiaicomtat.backend.assertions.assertFailure
import com.arnyminerz.escalaralcoiaicomtat.backend.assertions.assertSuccess
import com.arnyminerz.escalaralcoiaicomtat.backend.server.base.ApplicationTestBase
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.DataProvider
import com.arnyminerz.escalaralcoiaicomtat.backend.server.error.Errors
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.getStringOrNull
import io.ktor.client.request.get
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestAreaFetchingEndpoint: ApplicationTestBase() {
    @Test
    fun `test getting area`() = test {
        val areaId = DataProvider.provideSampleArea()
        assertNotNull(areaId)

        var image: String? = null

        client.get("/area/$areaId").apply {
            assertSuccess { data ->
                assertNotNull(data)

                assertEquals(areaId, data.getInt("id"))
                assertEquals(DataProvider.SampleArea.displayName, data.getString("display_name"))
                assertEquals(DataProvider.SampleArea.webUrl, data.getString("web_url"))
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
            }
        }
    }

    @Test
    fun `test getting area - doesn't exist`() = test {
        client.get("/area/123").apply {
            assertFailure(Errors.ObjectNotFound)
        }
    }
}
