package com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.info

import com.arnyminerz.escalaralcoiaicomtat.backend.assertions.assertSuccess
import com.arnyminerz.escalaralcoiaicomtat.backend.server.base.ApplicationTestBase
import com.arnyminerz.escalaralcoiaicomtat.backend.system.Package
import io.ktor.client.request.get
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

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
