package server.endpoints.delete

import ServerDatabase
import assertions.assertFailure
import assertions.assertSuccess
import database.EntityTypes
import database.entity.Zone
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

class TestDeleteZoneEndpoint: ApplicationTestBase() {
    @Test
    fun `test deleting Zone`() = test {
        val areaId = DataProvider.provideSampleArea()
        val zoneId = DataProvider.provideSampleZone(areaId)

        val lastUpdate = ServerDatabase.instance.query { LastUpdate.get() }

        assertNotNull(zoneId)

        val zone = ServerDatabase.instance.query { Zone[zoneId] }
        val image = zone.image
        val kmz = zone.kmz

        assertTrue { image.exists() }
        assertTrue { kmz.exists() }

        client.delete("/zone/$zoneId") {
            header(HttpHeaders.Authorization, "Bearer $AUTH_TOKEN")
        }.apply {
            assertSuccess()
        }

        assertFalse { image.exists() }
        assertFalse { kmz.exists() }

        ServerDatabase.instance.query { assertNotEquals(LastUpdate.get(), lastUpdate) }

        client.get("/zone/$zoneId") {
            header(HttpHeaders.Authorization, "Bearer $AUTH_TOKEN")
        }.apply {
            assertFailure(Errors.ObjectNotFound)
        }

        assertNotificationSent(Notifier.TOPIC_DELETED, EntityTypes.ZONE, zoneId)
    }

    @Test
    fun `test deleting non existing Zone`() = test {
        client.delete("/zone/123") {
            header(HttpHeaders.Authorization, "Bearer $AUTH_TOKEN")
        }.apply {
            assertFailure(Errors.ObjectNotFound)
        }
        assertNotificationNotSent(Notifier.TOPIC_DELETED, EntityTypes.ZONE)
    }
}
