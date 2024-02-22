package server.endpoints.query

import assertions.assertFailure
import assertions.assertIsUUID
import assertions.assertSuccess
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
