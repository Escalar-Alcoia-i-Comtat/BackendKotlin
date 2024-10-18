package server.endpoints.create

import ServerDatabase
import assertions.assertFailure
import database.EntityTypes
import database.entity.Zone
import database.entity.info.LastUpdate
import distribution.Notifier
import java.net.URL
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import server.DataProvider
import server.base.ApplicationTestBase
import server.error.Errors

class TestZoneCreationEndpoint : ApplicationTestBase() {
    @Test
    fun `test zone creation`() = test {
        val lastUpdate = ServerDatabase.instance.query { LastUpdate.get() }

        val areaId: Int? = DataProvider.provideSampleArea(this)
        assertNotNull(areaId)

        val zoneId: Int? = DataProvider.provideSampleZone(this, areaId)
        assertNotNull(zoneId)

        ServerDatabase.instance.query {
            val zone = Zone[zoneId]
            assertNotNull(zone)
            assertEquals(DataProvider.SampleZone.displayName, zone.displayName)
            assertEquals(URL(DataProvider.SampleZone.webUrl), zone.webUrl)
            assertEquals(DataProvider.SampleZone.point, zone.point)
            assertEquals(DataProvider.SampleZone.points, zone.points)

            val imageFile = zone.image
            assertTrue(imageFile.exists())

            val kmzFile = zone.kmz
            assertTrue(kmzFile.exists())

            assertNotEquals(LastUpdate.get(), lastUpdate)
        }

        assertNotificationSent(Notifier.TOPIC_CREATED, EntityTypes.ZONE, zoneId)
    }

    @Test
    fun `test zone creation - missing arguments`() = test {
        val areaId = DataProvider.provideSampleArea(this)
        DataProvider.provideSampleZone(this, areaId, skipDisplayName = true) {
            assertFailure(Errors.MissingData)
            assertNotificationNotSent(Notifier.TOPIC_CREATED, EntityTypes.ZONE)
            null
        }
        DataProvider.provideSampleZone(this, areaId, skipImage = true) {
            assertFailure(Errors.MissingData)
            assertNotificationNotSent(Notifier.TOPIC_CREATED, EntityTypes.ZONE)
            null
        }
        DataProvider.provideSampleZone(this, areaId, skipWebUrl = true) {
            assertFailure(Errors.MissingData)
            assertNotificationNotSent(Notifier.TOPIC_CREATED, EntityTypes.ZONE)
            null
        }
        DataProvider.provideSampleZone(this, areaId, skipKmz = true) {
            assertFailure(Errors.MissingData)
            assertNotificationNotSent(Notifier.TOPIC_CREATED, EntityTypes.ZONE)
            null
        }
    }

    @Test
    fun `test zone creation - no points`() = test {
        val areaId = DataProvider.provideSampleArea(this)
        assertNotNull(areaId)

        val zoneId = DataProvider.provideSampleZone(this, areaId, emptyPoints = true)
        assertNotNull(zoneId)

        ServerDatabase.instance.query {
            val zone = Zone[zoneId]
            assertNotNull(zone)
            assertTrue(zone.points.isEmpty())
        }

        assertNotificationSent(Notifier.TOPIC_CREATED, EntityTypes.ZONE, zoneId)
    }

    @Test
    fun `test zone creation - invalid zone id`() = test {
        DataProvider.provideSampleZone(this, 123) {
            assertFailure(Errors.ParentNotFound)
            assertNotificationNotSent(Notifier.TOPIC_CREATED, EntityTypes.ZONE)
            null
        }
    }
}
