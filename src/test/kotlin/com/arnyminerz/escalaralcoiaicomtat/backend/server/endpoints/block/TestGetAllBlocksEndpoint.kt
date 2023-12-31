package com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.block

import com.arnyminerz.escalaralcoiaicomtat.backend.assertions.assertSuccess
import com.arnyminerz.escalaralcoiaicomtat.backend.data.BlockingTypes
import com.arnyminerz.escalaralcoiaicomtat.backend.server.DataProvider
import com.arnyminerz.escalaralcoiaicomtat.backend.server.base.ApplicationTestBase
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.getEnumOrNull
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.getIntOrNull
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.getJSONObjectOrNull
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.getLongOrNull
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.getStringOrNull
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.jsonOf
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class TestGetAllBlocksEndpoint: ApplicationTestBase() {
    @Test
    fun `test get all blocks`() = test {
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

        get("/blocks").apply {
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
