package com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.query

import com.arnyminerz.escalaralcoiaicomtat.backend.assertions.assertFailure
import com.arnyminerz.escalaralcoiaicomtat.backend.assertions.assertIsUUID
import com.arnyminerz.escalaralcoiaicomtat.backend.assertions.assertSuccess
import com.arnyminerz.escalaralcoiaicomtat.backend.server.DataProvider
import com.arnyminerz.escalaralcoiaicomtat.backend.server.base.ApplicationTestBase
import com.arnyminerz.escalaralcoiaicomtat.backend.server.error.Errors
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.getLongOrNull
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.getStringOrNull
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

        get("/area/$areaId").apply {
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
    fun `test getting area - doesn't exist`() = test {
        get("/area/123").apply {
            assertFailure(Errors.ObjectNotFound)
        }
    }
}
