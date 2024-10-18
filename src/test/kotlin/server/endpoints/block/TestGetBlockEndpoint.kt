package server.endpoints.block

import ServerDatabase
import assertions.assertSuccess
import data.BlockingTypes
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import server.DataProvider
import server.base.ApplicationTestBase
import server.request.AddBlockRequest
import server.response.query.BlocksResponseData

class TestGetBlockEndpoint: ApplicationTestBase() {
    @Test
    fun `test get path's block`() = test {
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

        get("/block/$pathId").apply {
            assertSuccess<BlocksResponseData> { data ->
                assertNotNull(data)

                val blocks = data.blocks
                assertEquals(1, blocks.size)

                val block = blocks[0]
                assertNotNull(block.id)
                assertNotNull(block.timestamp)
                assertEquals(BlockingTypes.BUILD, block.type)
                assertNull(block.recurrence)
                assertNull(block.endDate)
                ServerDatabase.instance { assertEquals(pathId, block.path.id.value) }
            }
        }
    }
}
