package server.endpoints.info

import assertions.assertSuccess
import io.ktor.client.request.get
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import server.base.ApplicationTestBase
import system.Package

class TestServerInfoEndpoint : ApplicationTestBase() {
    @Test
    fun `test server information`() = test {
        client.get("/info").apply {
            assertSuccess { data ->
                assertNotNull(data)

                data["version"].let { version ->
                    assertNotNull(version)
                    assertEquals(Package.getVersion(), version)
                }
            }
        }
    }
}
