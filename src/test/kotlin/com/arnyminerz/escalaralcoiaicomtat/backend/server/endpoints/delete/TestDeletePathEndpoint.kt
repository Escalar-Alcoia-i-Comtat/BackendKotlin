package com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.delete

import com.arnyminerz.escalaralcoiaicomtat.backend.assertions.assertFailure
import com.arnyminerz.escalaralcoiaicomtat.backend.assertions.assertSuccess
import com.arnyminerz.escalaralcoiaicomtat.backend.server.DataProvider
import com.arnyminerz.escalaralcoiaicomtat.backend.server.base.ApplicationTestBase
import com.arnyminerz.escalaralcoiaicomtat.backend.server.error.Errors
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import kotlin.test.Test

class TestDeletePathEndpoint: ApplicationTestBase() {
    @Test
    fun `test deleting Path`() = test {
        val areaId = DataProvider.provideSampleArea()
        val zoneId = DataProvider.provideSampleZone(areaId)
        val sectorId = DataProvider.provideSampleSector(zoneId)
        val pathId = DataProvider.provideSamplePath(sectorId)

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
    }
}
