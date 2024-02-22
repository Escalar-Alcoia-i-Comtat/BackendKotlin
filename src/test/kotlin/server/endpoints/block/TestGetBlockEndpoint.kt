package server.endpoints.block

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
import utils.getEnumOrNull
import utils.getIntOrNull
import utils.getJSONObjectOrNull
import utils.getLongOrNull
import utils.getStringOrNull
import utils.jsonOf

class TestGetBlockEndpoint: ApplicationTestBase() {
    @Test
    fun `test get path's block`() = test {
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

        get("/block/$pathId").apply {
            assertSuccess { data ->
                assertNotNull(data)

                val blocksJson = data.getJSONArray("blocks")
                assertEquals(1, blocksJson.length())

                val block = blocksJson.getJSONObject(0)
                assertNotNull(block.getIntOrNull("id"))
                assertNotNull(block.getLongOrNull("timestamp"))
                assertEquals(BlockingTypes.BUILD, block.getEnumOrNull(BlockingTypes::class, "type"))
                assertNull(block.getJSONObjectOrNull("recurrence"))
                assertNull(block.getStringOrNull("end_date"))
                assertEquals(pathId, block.getIntOrNull("path_id"))
            }
        }
    }
}
