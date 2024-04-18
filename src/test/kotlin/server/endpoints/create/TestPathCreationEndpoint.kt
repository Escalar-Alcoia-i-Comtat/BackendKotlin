package server.endpoints.create

import ServerDatabase
import assertions.assertFailure
import database.EntityTypes
import database.entity.Path
import database.entity.info.LastUpdate
import database.table.Paths
import distribution.Notifier
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import server.DataProvider
import server.base.ApplicationTestBase
import server.error.Errors

class TestPathCreationEndpoint: ApplicationTestBase() {
    @Test
    fun `test path creation`() = test {
        val lastUpdate = ServerDatabase.instance.query { LastUpdate.get() }

        val areaId: Int? = DataProvider.provideSampleArea()
        assertNotNull(areaId)

        val zoneId: Int? = DataProvider.provideSampleZone(areaId)
        assertNotNull(zoneId)

        val sectorId: Int? = DataProvider.provideSampleSector(zoneId)
        assertNotNull(sectorId)

        val pathId: Int? = DataProvider.provideSamplePath(sectorId)
        assertNotNull(pathId)

        ServerDatabase.instance.query {
            val path = Path[pathId]
            assertNotNull(path)
            assertEquals(DataProvider.SamplePath.displayName, path.displayName)
            assertEquals(DataProvider.SamplePath.sketchId, path.sketchId)

            assertEquals(DataProvider.SamplePath.height, path.height)
            assertEquals(DataProvider.SamplePath.grade, path.grade)
            assertEquals(DataProvider.SamplePath.ending, path.ending)

            assertContentEquals(DataProvider.SamplePath.pitches, path.pitches)

            assertEquals(DataProvider.SamplePath.stringCount, path.stringCount)

            assertEquals(DataProvider.SamplePath.paraboltCount, path.paraboltCount)
            assertEquals(DataProvider.SamplePath.burilCount, path.burilCount)
            assertEquals(DataProvider.SamplePath.pitonCount, path.pitonCount)
            assertEquals(DataProvider.SamplePath.spitCount, path.spitCount)
            assertEquals(DataProvider.SamplePath.tensorCount, path.tensorCount)

            assertEquals(DataProvider.SamplePath.crackerRequired, path.crackerRequired)
            assertEquals(DataProvider.SamplePath.friendRequired, path.friendRequired)
            assertEquals(DataProvider.SamplePath.lanyardRequired, path.lanyardRequired)
            assertEquals(DataProvider.SamplePath.nailRequired, path.nailRequired)
            assertEquals(DataProvider.SamplePath.pitonRequired, path.pitonRequired)
            assertEquals(DataProvider.SamplePath.stapesRequired, path.stapesRequired)

            assertEquals(DataProvider.SamplePath.builder, path.builder)
            assertContentEquals(DataProvider.SamplePath.reBuilder, path.reBuilder)

            assertNull(path.images)

            assertNotEquals(LastUpdate.get(), lastUpdate)
        }

        assertNotificationSent(Notifier.TOPIC_CREATED, EntityTypes.PATH, pathId)
    }

    @Test
    fun `test path creation - with image`() = test {
        val areaId: Int? = DataProvider.provideSampleArea()
        assertNotNull(areaId)

        val zoneId: Int? = DataProvider.provideSampleZone(areaId)
        assertNotNull(zoneId)

        val sectorId: Int? = DataProvider.provideSampleSector(zoneId)
        assertNotNull(sectorId)

        // Include only an image, using uixola as sample
        val pathId: Int? = DataProvider.provideSamplePath(sectorId, images = listOf("/images/uixola.jpg"))
        assertNotNull(pathId)

        ServerDatabase.instance.query {
            val path = Path[pathId]
            assertNotNull(path)

            val images = path.images
            assertNotNull(images)
            assertEquals(1, images.size)
            assertTrue(images[0].exists())
        }

        assertNotificationSent(Notifier.TOPIC_CREATED, EntityTypes.PATH, pathId)
    }

    @Test
    fun `test path creation - with multiple images`() = test {
        val areaId: Int? = DataProvider.provideSampleArea()
        assertNotNull(areaId)

        val zoneId: Int? = DataProvider.provideSampleZone(areaId)
        assertNotNull(zoneId)

        val sectorId: Int? = DataProvider.provideSampleSector(zoneId)
        assertNotNull(sectorId)

        // Include only an image, using uixola as sample
        val pathId: Int? = DataProvider.provideSamplePath(
            sectorId,
            images = listOf("/images/uixola.jpg", "/images/uixola.jpg")
        )
        assertNotNull(pathId)

        ServerDatabase.instance.query {
            val path = Path[pathId]
            assertNotNull(path)

            val images = path.images
            assertNotNull(images)
            assertEquals(2, images.size)
            assertTrue(images[0].exists())
            assertTrue(images[1].exists())
        }

        assertNotificationSent(Notifier.TOPIC_CREATED, EntityTypes.PATH, pathId)
    }

    @Test
    fun `test path creation - missing arguments`() = test {
        val areaId = DataProvider.provideSampleArea()
        val zoneId: Int? = DataProvider.provideSampleZone(areaId)
        val sectorId: Int? = DataProvider.provideSampleSector(zoneId)
        DataProvider.provideSamplePath(sectorId, skipDisplayName = true) {
            assertFailure(Errors.MissingData)
            null
        }
        DataProvider.provideSamplePath(sectorId, skipSketchId = true) {
            assertFailure(Errors.MissingData)
            null
        }

        assertNotificationNotSent(Notifier.TOPIC_CREATED, EntityTypes.PATH)
    }

    @Test
    fun `test path creation - invalid zone id`() = test {
        DataProvider.provideSamplePath(123) {
            assertFailure(Errors.ParentNotFound)
            null
        }

        assertNotificationNotSent(Notifier.TOPIC_CREATED, EntityTypes.PATH)
    }

    @Test
    fun `test path creation - too many images`() = test {
        val areaId = DataProvider.provideSampleArea()
        val zoneId: Int? = DataProvider.provideSampleZone(areaId)
        val sectorId: Int? = DataProvider.provideSampleSector(zoneId)
        DataProvider.provideSamplePath(
            sectorId,
            images = arrayOfNulls<String>(Paths.MAX_IMAGES + 1)
                .map { "/images/uixola.jpg" }
                .toList()
        ) {
            assertFailure(Errors.TooManyImages)
            null
        }

        assertNotificationNotSent(Notifier.TOPIC_CREATED, EntityTypes.PATH)
    }
}
