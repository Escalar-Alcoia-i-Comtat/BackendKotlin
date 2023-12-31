package com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.block

import com.arnyminerz.escalaralcoiaicomtat.backend.ServerDatabase
import com.arnyminerz.escalaralcoiaicomtat.backend.assertions.assertSuccess
import com.arnyminerz.escalaralcoiaicomtat.backend.data.BlockingTypes
import com.arnyminerz.escalaralcoiaicomtat.backend.database.entity.Blocking
import com.arnyminerz.escalaralcoiaicomtat.backend.database.entity.info.LastUpdate
import com.arnyminerz.escalaralcoiaicomtat.backend.database.table.BlockingTable
import com.arnyminerz.escalaralcoiaicomtat.backend.server.DataProvider
import com.arnyminerz.escalaralcoiaicomtat.backend.server.base.ApplicationTestBase
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.jsonOf
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

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
