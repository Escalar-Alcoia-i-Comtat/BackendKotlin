package server.endpoints.info

import ServerDatabase
import assertions.assertSuccess
import io.ktor.client.request.get
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import server.base.ApplicationTestBase
import server.response.query.ServerInfoResponseData
import system.Package

class TestServerInfoEndpoint : ApplicationTestBase() {
    @Test
    fun `test server information`() = test {
        client.get("/info").apply {
            assertSuccess<ServerInfoResponseData> { data ->
                assertNotNull(data)

                data.version.let { version ->
                    assertNotNull(version)
                    assertEquals(Package.getVersion(), version)
                }
                data.databaseVersion.let {
                    assertNotNull(it)
                    assertEquals(ServerDatabase.VERSION, it)
                }
            }
        }
    }
}
