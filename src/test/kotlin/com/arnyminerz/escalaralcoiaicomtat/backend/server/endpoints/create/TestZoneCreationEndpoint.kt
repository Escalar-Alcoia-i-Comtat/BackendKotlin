package com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.create

import com.arnyminerz.escalaralcoiaicomtat.backend.ServerDatabase
import com.arnyminerz.escalaralcoiaicomtat.backend.assertions.assertFailure
import com.arnyminerz.escalaralcoiaicomtat.backend.database.entity.Zone
import com.arnyminerz.escalaralcoiaicomtat.backend.database.entity.info.LastUpdate
import com.arnyminerz.escalaralcoiaicomtat.backend.server.DataProvider
import com.arnyminerz.escalaralcoiaicomtat.backend.server.base.ApplicationTestBase
import com.arnyminerz.escalaralcoiaicomtat.backend.server.error.Errors
import java.net.URL
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

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
