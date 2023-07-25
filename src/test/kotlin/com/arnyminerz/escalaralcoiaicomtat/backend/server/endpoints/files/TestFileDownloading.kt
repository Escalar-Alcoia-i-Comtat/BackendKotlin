package com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.files

import com.arnyminerz.escalaralcoiaicomtat.backend.assertions.assertSuccess
import com.arnyminerz.escalaralcoiaicomtat.backend.server.base.ApplicationTestBase
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.DataProvider
import io.ktor.client.request.get
import io.ktor.client.statement.readBytes
import kotlin.test.Test
import kotlin.test.assertNotNull

class TestFileDownloading : ApplicationTestBase() {
    @Test
    fun `test downloading files`() = test {
        val areaId = DataProvider.provideSampleArea()

        var image: String? = null

        client.get("/area/$areaId").apply {
            assertSuccess { data ->
                assertNotNull(data)
                image = data.getString("image")
            }
        }

        assertNotNull(image)

        client.get("/download/$image").apply {
            readBytes()
        }
    }
}
