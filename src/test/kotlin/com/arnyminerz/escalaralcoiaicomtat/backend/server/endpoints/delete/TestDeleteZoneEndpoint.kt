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

class TestDeleteZoneEndpoint: ApplicationTestBase() {
    @Test
    fun `test deleting Zone`() = test {
        val areaId = DataProvider.provideSampleArea()
        val zoneId = DataProvider.provideSampleZone(areaId)

        client.delete("/zone/$zoneId") {
            header(HttpHeaders.Authorization, "Bearer $AUTH_TOKEN")
        }.apply {
            assertSuccess()
        }
        
        client.get("/zone/$zoneId") {
            header(HttpHeaders.Authorization, "Bearer $AUTH_TOKEN")
        }.apply {
            assertFailure(Errors.ObjectNotFound)
        }
    }
}
