package com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints

import com.arnyminerz.escalaralcoiaicomtat.backend.assertions.assertFailure
import com.arnyminerz.escalaralcoiaicomtat.backend.assertions.assertSuccess
import com.arnyminerz.escalaralcoiaicomtat.backend.server.base.ApplicationTestBase
import com.arnyminerz.escalaralcoiaicomtat.backend.server.error.Errors
import io.ktor.client.request.get
import kotlin.test.Test

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
