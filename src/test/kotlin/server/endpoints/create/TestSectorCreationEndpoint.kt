package server.endpoints.create

import ServerDatabase
import assertions.assertFailure
import database.EntityTypes
import database.entity.Sector
import database.entity.info.LastUpdate
import distribution.Notifier
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import server.DataProvider
import server.base.ApplicationTestBase
import server.error.Errors

class TestSectorCreationEndpoint: ApplicationTestBase() {
    @Test
    fun `test sector creation`() = test {
        val lastUpdate = ServerDatabase.instance.query { LastUpdate.get() }

        val areaId: Int? = DataProvider.provideSampleArea()
        assertNotNull(areaId)

        val zoneId: Int? = DataProvider.provideSampleZone(areaId)
        assertNotNull(zoneId)

        val sectorId: Int? = DataProvider.provideSampleSector(zoneId)
        assertNotNull(sectorId)

        ServerDatabase.instance.query {
            val sector = Sector[sectorId]
            assertNotNull(sector)
            assertEquals(DataProvider.SampleSector.displayName, sector.displayName)
            assertEquals(DataProvider.SampleSector.point, sector.point)
            assertEquals(DataProvider.SampleSector.kidsApt, sector.kidsApt)
            assertEquals(DataProvider.SampleSector.walkingTime, sector.walkingTime)
            assertEquals(DataProvider.SampleSector.sunTime, sector.sunTime)

            val imageFile = sector.image
            assertTrue(imageFile.exists())

            val gpxFile = sector.gpx
            assertNotNull(gpxFile)
            assertTrue(gpxFile.exists())

            assertNotEquals(LastUpdate.get(), lastUpdate)
        }

        assertNotificationSent(Notifier.TOPIC_CREATED, EntityTypes.SECTOR, sectorId)
    }

    @Test
    fun `test sector creation - without gpx`() = test {
        val lastUpdate = ServerDatabase.instance.query { LastUpdate.get() }

        val areaId: Int? = DataProvider.provideSampleArea()
        assertNotNull(areaId)

        val zoneId: Int? = DataProvider.provideSampleZone(areaId)
        assertNotNull(zoneId)

        val sectorId: Int? = DataProvider.provideSampleSector(zoneId, skipGpx = true)
        assertNotNull(sectorId)

        ServerDatabase.instance.query {
            val sector = Sector[sectorId]
            assertNotNull(sector)
            assertEquals(DataProvider.SampleSector.displayName, sector.displayName)
            assertEquals(DataProvider.SampleSector.point, sector.point)
            assertEquals(DataProvider.SampleSector.kidsApt, sector.kidsApt)
            assertEquals(DataProvider.SampleSector.walkingTime, sector.walkingTime)
            assertEquals(DataProvider.SampleSector.sunTime, sector.sunTime)

            val imageFile = sector.image
            assertTrue(imageFile.exists())

            val gpxFile = sector.gpx
            assertNull(gpxFile)

            assertNotEquals(LastUpdate.get(), lastUpdate)
        }

        assertNotificationSent(Notifier.TOPIC_CREATED, EntityTypes.SECTOR, sectorId)
    }

    @Test
    fun `test sector creation - missing arguments`() = test {
        val areaId = DataProvider.provideSampleArea()
        val zoneId = DataProvider.provideSampleZone(areaId)
        DataProvider.provideSampleSector(null) {
            assertFailure(Errors.MissingData)
            assertNotificationNotSent(Notifier.TOPIC_CREATED, EntityTypes.SECTOR)
            null
        }
        DataProvider.provideSampleSector(zoneId, skipDisplayName = true) {
            assertFailure(Errors.MissingData)
            assertNotificationNotSent(Notifier.TOPIC_CREATED, EntityTypes.SECTOR)
            null
        }
        DataProvider.provideSampleSector(zoneId, skipImage = true) {
            assertFailure(Errors.MissingData)
            assertNotificationNotSent(Notifier.TOPIC_CREATED, EntityTypes.SECTOR)
            null
        }
        DataProvider.provideSampleSector(zoneId, skipKidsApt = true) {
            assertFailure(Errors.MissingData)
            assertNotificationNotSent(Notifier.TOPIC_CREATED, EntityTypes.SECTOR)
            null
        }
        DataProvider.provideSampleSector(areaId, skipSunTime = true) {
            assertFailure(Errors.MissingData)
            assertNotificationNotSent(Notifier.TOPIC_CREATED, EntityTypes.SECTOR)
            null
        }
    }

    @Test
    fun `test sector creation - invalid zone id`() = test {
        DataProvider.provideSampleSector(123) {
            assertFailure(Errors.ParentNotFound)
            assertNotificationNotSent(Notifier.TOPIC_CREATED, EntityTypes.SECTOR)
            null
        }
    }
}
