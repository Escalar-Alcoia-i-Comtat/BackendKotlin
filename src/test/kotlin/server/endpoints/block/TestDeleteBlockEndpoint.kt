package server.endpoints.block

import ServerDatabase
import assertions.assertSuccess
import data.BlockingTypes
import database.EntityTypes
import database.entity.Blocking
import database.entity.info.LastUpdate
import database.table.BlockingTable
import distribution.Notifier
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import server.DataProvider
import server.base.ApplicationTestBase
import server.request.AddBlockRequest

class TestDeleteBlockEndpoint: ApplicationTestBase() {
    @Test
    fun `test deleting path's block`() = test {
        val areaId = with(DataProvider) { provideSampleArea() }
        val zoneId = with(DataProvider) { provideSampleZone(areaId) }
        val sectorId = with(DataProvider) { provideSampleSector(zoneId) }
        val pathId = with(DataProvider) { provideSamplePath(sectorId) }

        post("/block/$pathId") {
            setBody(
                AddBlockRequest(BlockingTypes.BUILD)
            )
        }.apply {
            assertSuccess(HttpStatusCode.Created)
        }

        val lastUpdate = ServerDatabase.instance.query { LastUpdate.get() }

        var blockId: Int? = null
        val block = ServerDatabase.instance.query {
            Blocking.find { BlockingTable.path eq pathId }.firstOrNull()?.also { blockId = it.path.id.value }
        }
        assertNotNull(block)

        delete("/block/${block.id}").apply {
            assertSuccess()
        }

        ServerDatabase.instance.query {
            Blocking.findById(block.id)
        }?.let {
            assertNull(it)
        }

        ServerDatabase.instance.query { assertNotEquals(LastUpdate.get(), lastUpdate) }

        assertNotNull(blockId)
        assertNotificationSent(Notifier.TOPIC_DELETED, EntityTypes.BLOCKING, blockId!!)
    }
}
