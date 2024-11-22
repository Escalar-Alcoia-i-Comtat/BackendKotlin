package server.endpoints.files

import ServerDatabase
import assertions.assertFailure
import assertions.assertSuccess
import database.entity.Area
import database.entity.Zone
import server.DataProvider
import server.base.ApplicationTestBase
import server.error.Errors
import server.response.files.RequestFilesResponseData
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestFilesFetching : ApplicationTestBase() {
    @Test
    fun `test data`() = test {
        val areaId = DataProvider.provideSampleArea(this)
        assertNotNull(areaId)

        val area: Area = ServerDatabase.instance.query { Area[areaId] }

        get("/files/${area.image.name}").apply {
            assertSuccess<RequestFilesResponseData> { data ->
                assertNotNull(data)
            }
        }
    }

    @Test
    fun `test data no extension`() = test {
        val areaId = DataProvider.provideSampleArea(this)
        assertNotNull(areaId)

        val area: Area = ServerDatabase.instance.query { Area[areaId] }

        get("/files/${area.image.nameWithoutExtension}").apply {
            assertSuccess<RequestFilesResponseData> { data ->
                assertNotNull(data)
            }
        }
    }

    @Test
    fun `test doesn't exist`() = test {
        get("/files/unknown").apply {
            assertFailure(Errors.FileNotFound)
        }
    }

    @Test
    fun `test data multiple`() = test {
        val areaId = DataProvider.provideSampleArea(this)
        assertNotNull(areaId)
        val zoneId = DataProvider.provideSampleZone(this, areaId)
        assertNotNull(zoneId)

        val area: Area = ServerDatabase.instance.query { Area[areaId] }
        val zone: Zone = ServerDatabase.instance.query { Zone[zoneId] }

        get("/files/${area.image.name},${zone.image.name}").apply {
            assertSuccess<RequestFilesResponseData> { data ->
                assertNotNull(data)

                val files = data.files
                assertTrue(files.isNotEmpty())
            }
        }
    }
}
