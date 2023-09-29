package com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.create

import com.arnyminerz.escalaralcoiaicomtat.backend.ServerDatabase
import com.arnyminerz.escalaralcoiaicomtat.backend.assertions.assertFailure
import com.arnyminerz.escalaralcoiaicomtat.backend.database.entity.Path
import com.arnyminerz.escalaralcoiaicomtat.backend.database.entity.info.LastUpdate
import com.arnyminerz.escalaralcoiaicomtat.backend.database.table.Paths
import com.arnyminerz.escalaralcoiaicomtat.backend.server.DataProvider
import com.arnyminerz.escalaralcoiaicomtat.backend.server.base.ApplicationTestBase
import com.arnyminerz.escalaralcoiaicomtat.backend.server.error.Errors
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

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
    }

    @Test
    fun `test path creation - invalid zone id`() = test {
        DataProvider.provideSamplePath(123) {
            assertFailure(Errors.ParentNotFound)
            null
        }
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
    }
}
