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

class TestGetAllBlocksEndpoint : ApplicationTestBase() {
    @Test
    fun `test get all blocks`() = test {
        val areaId = DataProvider.provideSampleArea(this)
        val zoneId = DataProvider.provideSampleZone(this, areaId)
        val sectorId = DataProvider.provideSampleSector(this, zoneId)
        val pathId = DataProvider.provideSamplePath(this, sectorId)

        post("/block/$pathId") {
            setBody(
                AddBlockRequest(BlockingTypes.BUILD)
            )
        }.apply {
            assertSuccess(HttpStatusCode.Created)
        }

        get("/blocks").apply {
            assertSuccess<BlocksResponseData> { data ->
                assertNotNull(data)

                val blocks = data.blocks
                assertEquals(1, blocks.size)

                val block = blocks[0]
                assertNotNull(block.id.value)
                assertNotNull(block.timestamp)
                assertEquals(BlockingTypes.BUILD, block.type)
                assertNull(block.recurrence)
                assertNull(block.endDate)
                ServerDatabase.instance { assertEquals(pathId, block.path.id.value) }
            }
        }
    }
}
