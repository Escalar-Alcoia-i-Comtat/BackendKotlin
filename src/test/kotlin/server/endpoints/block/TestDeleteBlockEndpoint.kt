package server.endpoints.block

import ServerDatabase
import assertions.assertSuccess
import data.BlockingTypes
import database.entity.Blocking
import database.entity.info.LastUpdate
import database.table.BlockingTable
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import server.DataProvider
import server.base.ApplicationTestBase
import utils.jsonOf

class TestDeleteBlockEndpoint: ApplicationTestBase() {
    @Test
    fun `test deleting path's block`() = test {
        val areaId = DataProvider.provideSampleArea()
        val zoneId = DataProvider.provideSampleZone(areaId)
        val sectorId = DataProvider.provideSampleSector(zoneId)
        val pathId = DataProvider.provideSamplePath(sectorId)

        post("/block/$pathId") {
            setBody(
                jsonOf(
                    "type" to BlockingTypes.BUILD
                ).toString()
            )
        }.apply {
            assertSuccess(HttpStatusCode.Created)
        }

        val lastUpdate = ServerDatabase.instance.query { LastUpdate.get() }

        val block = ServerDatabase.instance.query {
            Blocking.find { BlockingTable.path eq pathId }.firstOrNull()
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
    }
}
