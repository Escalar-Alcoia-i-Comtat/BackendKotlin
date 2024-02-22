package server.endpoints.create

import ServerDatabase
import assertions.assertFailure
import database.entity.Zone
import database.entity.info.LastUpdate
import java.net.URL
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import server.DataProvider
import server.base.ApplicationTestBase
import server.error.Errors

class TestZoneCreationEndpoint: ApplicationTestBase() {
    @Test
    fun `test zone creation`() = test {
        val lastUpdate = ServerDatabase.instance.query { LastUpdate.get() }

        val areaId: Int? = DataProvider.provideSampleArea()
        assertNotNull(areaId)

        val zoneId: Int? = DataProvider.provideSampleZone(areaId)
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
    }

    @Test
    fun `test zone creation - missing arguments`() = test {
        val areaId = DataProvider.provideSampleArea()
        DataProvider.provideSampleZone(areaId, skipDisplayName = true) {
            assertFailure(Errors.MissingData)
            null
        }
        DataProvider.provideSampleZone(areaId, skipImage = true) {
            assertFailure(Errors.MissingData)
            null
        }
        DataProvider.provideSampleZone(areaId, skipWebUrl = true) {
            assertFailure(Errors.MissingData)
            null
        }
        DataProvider.provideSampleZone(areaId, skipKmz = true) {
            assertFailure(Errors.MissingData)
            null
        }
    }

    @Test
    fun `test zone creation - no points`() = test {
        val areaId = DataProvider.provideSampleArea()
        assertNotNull(areaId)

        val zoneId = DataProvider.provideSampleZone(areaId, emptyPoints = true)
        assertNotNull(zoneId)

        ServerDatabase.instance.query {
            val zone = Zone[zoneId]
            assertNotNull(zone)
            assertTrue(zone.points.isEmpty())
        }
    }

    @Test
    fun `test zone creation - invalid zone id`() = test {
        DataProvider.provideSampleZone(123) {
            assertFailure(Errors.ParentNotFound)
            null
        }
    }
}
