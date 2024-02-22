package server.endpoints.delete

import ServerDatabase
import assertions.assertFailure
import assertions.assertSuccess
import database.entity.info.LastUpdate
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import kotlin.test.Test
import kotlin.test.assertNotEquals
import server.DataProvider
import server.base.ApplicationTestBase
import server.error.Errors

class TestDeleteAreaEndpoint: ApplicationTestBase() {
    @Test
    fun `test deleting Area`() = test {
        val areaId = DataProvider.provideSampleArea()

        val lastUpdate = ServerDatabase.instance.query { LastUpdate.get() }

        client.delete("/area/$areaId") {
            header(HttpHeaders.Authorization, "Bearer $AUTH_TOKEN")
        }.apply {
            assertSuccess()
        }

        ServerDatabase.instance.query { assertNotEquals(LastUpdate.get(), lastUpdate) }

        client.get("/area/$areaId") {
            header(HttpHeaders.Authorization, "Bearer $AUTH_TOKEN")
        }.apply {
            assertFailure(Errors.ObjectNotFound)
        }
    }
}
