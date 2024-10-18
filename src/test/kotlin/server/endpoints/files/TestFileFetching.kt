package server.endpoints.files

import ServerDatabase
import assertions.assertFailure
import assertions.assertSuccess
import database.entity.Area
import database.entity.Zone
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import server.DataProvider
import server.base.ApplicationTestBase
import server.error.Errors
import server.response.files.RequestFileResponseData

class TestFileFetching : ApplicationTestBase() {
    @Test
    fun `test data`() = test {
        val areaId = with(DataProvider) { provideSampleArea() }
        assertNotNull(areaId)

        val area: Area = ServerDatabase.instance.query { Area[areaId] }

        get("/file/${area.image.name}").apply {
            assertSuccess<RequestFileResponseData> { data ->
                assertNotNull(data)
            }
        }
    }

    @Test
    fun `test data no extension`() = test {
        val areaId = with(DataProvider) { provideSampleArea() }
        assertNotNull(areaId)

        val area: Area = ServerDatabase.instance.query { Area[areaId] }

        get("/file/${area.image.nameWithoutExtension}").apply {
            assertSuccess<RequestFileResponseData> { data ->
                assertNotNull(data)
            }
        }
    }

    @Test
    fun `test doesn't exist`() = test {
        get("/file/unknown").apply {
            assertFailure(Errors.FileNotFound)
        }
    }

    @Test
    fun `test data multiple`() = test {
        val areaId = with(DataProvider) { provideSampleArea() }
        assertNotNull(areaId)
        val zoneId = with(DataProvider) { provideSampleZone(areaId) }
        assertNotNull(zoneId)

        val area: Area = ServerDatabase.instance.query { Area[areaId] }
        val zone: Zone = ServerDatabase.instance.query { Zone[zoneId] }

        get("/file/${area.image.name},${zone.image.name}").apply {
            assertSuccess<RequestFileResponseData> { data ->
                assertNotNull(data)

                val files = data.files
                assertTrue(files.isNotEmpty())
            }
        }
    }
}
