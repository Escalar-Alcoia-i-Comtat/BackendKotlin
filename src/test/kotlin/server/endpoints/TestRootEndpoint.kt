package server.endpoints

import assertions.assertFailure
import assertions.assertSuccess
import io.ktor.client.request.get
import kotlin.test.Test
import server.base.ApplicationTestBase
import server.error.Errors

class TestRootEndpoint : ApplicationTestBase() {
    @Test
    fun `test root`() = test {
        client.get("/").apply {
            assertSuccess()
        }
    }

    @Test
    fun `test 404`() = test {
        client.get("/not-found").apply {
            assertFailure(Errors.EndpointNotFound)
        }
    }
}
