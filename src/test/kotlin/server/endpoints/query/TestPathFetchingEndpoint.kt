package server.endpoints.query

import ServerDatabase
import assertions.assertFailure
import assertions.assertSuccess
import database.entity.Path
import io.ktor.http.HttpStatusCode
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import server.DataProvider
import server.base.ApplicationTestBase
import server.error.Errors
import server.response.files.RequestFileResponseData
import storage.Storage

class TestPathFetchingEndpoint : ApplicationTestBase() {
    @Test
    fun `test getting path`() = test {
        val areaId = DataProvider.provideSampleArea(this)
        assertNotNull(areaId)

        val zoneId = DataProvider.provideSampleZone(this, areaId)
        assertNotNull(zoneId)

        val sectorId = DataProvider.provideSampleSector(this, zoneId)
        assertNotNull(sectorId)

        val pathId = DataProvider.provideSamplePath(this, sectorId)
        assertNotNull(pathId)

        get("/path/$pathId").apply {
            assertSuccess<Path> { data ->
                assertNotNull(data)

                assertEquals(zoneId, data.id.value)
                assertEquals(sectorId, data.sector.id.value)

                assertEquals(DataProvider.SamplePath.displayName, data.displayName)
                assertEquals(DataProvider.SamplePath.sketchId, data.sketchId)

                assertEquals(DataProvider.SamplePath.height, data.height)
                assertEquals(DataProvider.SamplePath.grade, data.grade)
                assertEquals(DataProvider.SamplePath.ending, data.ending)

                assertContentEquals(DataProvider.SamplePath.pitches, data.pitches)

                assertEquals(DataProvider.SamplePath.stringCount, data.stringCount)

                assertEquals(DataProvider.SamplePath.paraboltCount, data.paraboltCount)
                assertEquals(DataProvider.SamplePath.burilCount, data.burilCount)
                assertEquals(DataProvider.SamplePath.pitonCount, data.pitonCount)
                assertEquals(DataProvider.SamplePath.spitCount, data.spitCount)
                assertEquals(DataProvider.SamplePath.tensorCount, data.tensorCount)

                assertEquals(DataProvider.SamplePath.crackerRequired, data.crackerRequired)
                assertEquals(DataProvider.SamplePath.friendRequired, data.friendRequired)
                assertEquals(DataProvider.SamplePath.lanyardRequired, data.lanyardRequired)
                assertEquals(DataProvider.SamplePath.nailRequired, data.nailRequired)
                assertEquals(DataProvider.SamplePath.pitonRequired, data.pitonRequired)
                assertEquals(DataProvider.SamplePath.stapesRequired, data.stapesRequired)

                assertEquals(DataProvider.SamplePath.builder, data.builder)
                assertContentEquals(DataProvider.SamplePath.reBuilder, data.reBuilder)

                assertTrue(data.timestamp < Instant.now())
            }
        }
    }

    @Test
    fun `test getting path - with image`() = test {
        val areaId = DataProvider.provideSampleArea(this)
        assertNotNull(areaId)

        val zoneId = DataProvider.provideSampleZone(this, areaId)
        assertNotNull(zoneId)

        val sectorId = DataProvider.provideSampleSector(this, zoneId)
        assertNotNull(sectorId)

        val pathId = DataProvider.provideSamplePath(this, sectorId, images = listOf("/images/uixola.jpg"))
        assertNotNull(pathId)

        var image: String? = null

        get("/path/$pathId").apply {
            assertSuccess<Path> { data ->
                assertNotNull(data)

                val images = data.images?.map { it.toRelativeString(Storage.ImagesDir) }
                assertNotNull(images)
                assertEquals(1, images.size)
                image = images[0]
            }
        }

        assertNotNull(image)

        get("/file/$image").apply {
            assertSuccess<RequestFileResponseData>()
        }
    }

    @Test
    fun `test getting path - doesn't exist`() = test {
        get("/path/123").apply {
            assertFailure(Errors.ObjectNotFound)
        }
    }

    @Test
    fun `test getting path - id NaN`() = test {
        get("/path/abc").apply {
            assertEquals(HttpStatusCode.BadRequest, status)
        }
    }

    @Test
    fun `test getting path - fix null builder`() = test {
        val areaId = DataProvider.provideSampleArea(this)
        assertNotNull(areaId)

        val zoneId = DataProvider.provideSampleZone(this, areaId)
        assertNotNull(zoneId)

        val sectorId = DataProvider.provideSampleSector(this, zoneId)
        assertNotNull(sectorId)

        val pathId = DataProvider.provideSamplePath(this, sectorId)
        assertNotNull(pathId)

        ServerDatabase.instance.query {
            Path[pathId]._builder = "null"
        }

        get("/path/$pathId").apply {
            assertSuccess()
        }

        ServerDatabase.instance.query {
            assertNull(Path[pathId].builder)
        }
    }

    @Test
    fun `test getting path - fix invalid pitches`() = test {
        val areaId = DataProvider.provideSampleArea(this)
        assertNotNull(areaId)

        val zoneId = DataProvider.provideSampleZone(this, areaId)
        assertNotNull(zoneId)

        val sectorId = DataProvider.provideSampleSector(this, zoneId)
        assertNotNull(sectorId)

        val pathId = DataProvider.provideSamplePath(this, sectorId)
        assertNotNull(pathId)

        ServerDatabase.instance.query {
            Path[pathId]._pitches = "{\"pitch\":\"0\"}"
        }

        get("/path/$pathId").apply {
            assertSuccess()
        }

        ServerDatabase.instance.query {
            assertNull(Path[pathId].pitches)
        }
    }
}
