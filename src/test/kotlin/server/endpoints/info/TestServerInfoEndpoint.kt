package server.endpoints.info

import ServerDatabase
import assertions.assertSuccess
import database.entity.info.LastUpdate
import io.ktor.client.request.get
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import server.base.ApplicationTestBase
import server.response.query.ServerInfoResponseData
import system.Package

class TestServerInfoEndpoint : ApplicationTestBase() {
    @Test
    fun `test server information`() = test {
        ServerDatabase.instance {
            LastUpdate.set(Instant.ofEpochSecond(1747072131))
        }

        client.get("/info").apply {
            assertSuccess<ServerInfoResponseData> { data ->
                assertNotNull(data)

                data.version.let { version ->
                    assertNotNull(version)
                    assertEquals(Package.getVersion(), version)
                }
                data.databaseVersion.let {
                    assertNotNull(it)
                    assertEquals(ServerDatabase.version, it)
                }
                data.lastUpdate.let {
                    assertNotNull(it)
                    assertEquals(1747072131, it.epochSecond)
                }
            }
        }
    }
}
