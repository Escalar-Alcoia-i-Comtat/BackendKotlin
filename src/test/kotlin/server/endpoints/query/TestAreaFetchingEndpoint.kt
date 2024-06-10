package server.endpoints.query

import assertions.assertFailure
import assertions.assertIsUUID
import assertions.assertSuccess
import database.entity.Area
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

class TestAreaFetchingEndpoint: ApplicationTestBase() {
    @Test
    fun `test getting area`() = test {
        val areaId = DataProvider.provideSampleArea()
        assertNotNull(areaId)

        var image: String? = null

        get("/area/$areaId").apply {
            assertSuccess<Area> { data ->
                assertNotNull(data)

                assertEquals(areaId, data.id.value)
                assertEquals(DataProvider.SampleArea.displayName, data.displayName)
                assertEquals(DataProvider.SampleArea.webUrl, data.webUrl.toString())
                assertTrue(data.timestamp < Instant.now())

                image = data.image.toRelativeString(Storage.ImagesDir)
            }
        }

        assertNotNull(image)
        assertIsUUID(image!!)

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
