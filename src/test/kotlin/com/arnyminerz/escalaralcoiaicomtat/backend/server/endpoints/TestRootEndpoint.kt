package com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints

import com.arnyminerz.escalaralcoiaicomtat.backend.assertions.assertSuccess
import com.arnyminerz.escalaralcoiaicomtat.backend.server.base.ApplicationTestBase
import io.ktor.client.request.get
import kotlin.test.Test

class TestRootEndpoint : ApplicationTestBase() {
    @Test
    fun `test root`() = test {
        client.get("/").apply {
            assertSuccess()
        }
    }
}
