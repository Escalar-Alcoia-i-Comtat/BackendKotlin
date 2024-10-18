package server.endpoints.delete

import ServerDatabase
import assertions.assertFailure
import assertions.assertSuccess
import data.BlockingTypes
import database.EntityTypes
import database.entity.Blocking
import database.entity.Path
import database.entity.info.LastUpdate
import distribution.Notifier
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import server.DataProvider
import server.base.ApplicationTestBase
import server.base.testDeleting
import server.base.testDeletingNotFound
import server.error.Errors

class TestDeletePathEndpoint : ApplicationTestBase() {
    @Test
    fun `test deleting Path`() = testDeleting(EntityTypes.PATH)

    @Test
    fun `test deleting Path with blocks`() = test {
        val areaId = DataProvider.provideSampleArea(this)
        val zoneId = DataProvider.provideSampleZone(this, areaId)
        val sectorId = DataProvider.provideSampleSector(this, zoneId)
        val pathId = DataProvider.provideSamplePath(this, sectorId)

        assertNotNull(pathId)

        val blockingId = ServerDatabase.instance.query {
            Blocking.new {
                type = BlockingTypes.BUILD
                path = Path.findById(pathId)!!
            }.id.value
        }

        client.delete("/path/$pathId") {
            header(HttpHeaders.Authorization, "Bearer $AUTH_TOKEN")
        }.apply {
            assertSuccess()
        }

        client.get("/path/$pathId") {
            header(HttpHeaders.Authorization, "Bearer $AUTH_TOKEN")
        }.apply {
            assertFailure(Errors.ObjectNotFound)
        }

        client.get("/block/$pathId") {
            header(HttpHeaders.Authorization, "Bearer $AUTH_TOKEN")
        }.apply {
            assertFailure(Errors.ObjectNotFound)
        }

        ServerDatabase.instance.query {
            val block = Blocking.findById(blockingId)
            assertNull(block)
        }

        assertNotificationSent(Notifier.TOPIC_DELETED, EntityTypes.PATH, pathId)
        assertNotificationSent(Notifier.TOPIC_DELETED, EntityTypes.BLOCKING, blockingId)
    }

    @Test
    fun `test deleting Path with images`() = test {
        val areaId = DataProvider.provideSampleArea(this)
        val zoneId = DataProvider.provideSampleZone(this, areaId)
        val sectorId = DataProvider.provideSampleSector(this, zoneId)
        val pathId = DataProvider.provideSamplePath(this, sectorId, images = listOf("/images/uixola.jpg"))

        val lastUpdate = ServerDatabase.instance.query { LastUpdate.get() }

        assertNotNull(pathId)
        val path = ServerDatabase.instance.query { Path[pathId] }
        val images = path.images
        assertNotNull(images)
        assertEquals(1, images.size)
        assertTrue(images[0].exists())

        client.delete("/path/$pathId") {
            header(HttpHeaders.Authorization, "Bearer $AUTH_TOKEN")
        }.apply {
            assertSuccess()
        }

        ServerDatabase.instance.query { assertNotEquals(LastUpdate.get(), lastUpdate) }

        client.get("/path/$pathId") {
            header(HttpHeaders.Authorization, "Bearer $AUTH_TOKEN")
        }.apply {
            assertFailure(Errors.ObjectNotFound)
        }
        assertFalse(images[0].exists())

        assertNotificationSent(Notifier.TOPIC_DELETED, EntityTypes.PATH, pathId)
    }

    @Test
    fun `test deleting non existing Path`() = testDeletingNotFound(EntityTypes.PATH)
}
