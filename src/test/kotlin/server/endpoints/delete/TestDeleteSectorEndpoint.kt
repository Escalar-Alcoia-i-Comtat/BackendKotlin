package server.endpoints.delete

import ServerDatabase
import assertions.assertFailure
import assertions.assertSuccess
import database.EntityTypes
import database.entity.Sector
import database.entity.info.LastUpdate
import distribution.Notifier
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import server.DataProvider
import server.base.ApplicationTestBase
import server.error.Errors

class TestDeleteSectorEndpoint: ApplicationTestBase() {
    @Test
    fun `test deleting Sector`() = test {
        val areaId = DataProvider.provideSampleArea()
        val zoneId = DataProvider.provideSampleZone(areaId)
        val sectorId = DataProvider.provideSampleSector(zoneId)

        val lastUpdate = ServerDatabase.instance.query { LastUpdate.get() }

        assertNotNull(sectorId)

        val sector = ServerDatabase.instance.query { Sector[sectorId] }
        val image = sector.image
        val gpx = sector.gpx

        assertTrue { image.exists() }
        assertTrue { gpx?.exists() != false }

        client.delete("/sector/$sectorId") {
            header(HttpHeaders.Authorization, "Bearer $AUTH_TOKEN")
        }.apply {
            assertSuccess()
        }

        assertFalse { image.exists() }
        assertFalse { gpx?.exists() != false }

        ServerDatabase.instance.query { assertNotEquals(LastUpdate.get(), lastUpdate) }

        client.get("/sector/$sectorId") {
            header(HttpHeaders.Authorization, "Bearer $AUTH_TOKEN")
        }.apply {
            assertFailure(Errors.ObjectNotFound)
        }

        assertNotificationSent(Notifier.TOPIC_DELETED, EntityTypes.SECTOR, sectorId)
    }

    @Test
    fun `test deleting non existing Sector`() = test {
        client.delete("/sector/123") {
            header(HttpHeaders.Authorization, "Bearer $AUTH_TOKEN")
        }.apply {
            assertFailure(Errors.ObjectNotFound)
        }
        assertNotificationNotSent(Notifier.TOPIC_DELETED, EntityTypes.SECTOR)
    }
}
