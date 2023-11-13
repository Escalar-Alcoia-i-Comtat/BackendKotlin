package com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.files

import com.arnyminerz.escalaralcoiaicomtat.backend.ServerDatabase
import com.arnyminerz.escalaralcoiaicomtat.backend.assertions.assertFailure
import com.arnyminerz.escalaralcoiaicomtat.backend.assertions.assertSuccess
import com.arnyminerz.escalaralcoiaicomtat.backend.database.entity.Area
import com.arnyminerz.escalaralcoiaicomtat.backend.server.DataProvider
import com.arnyminerz.escalaralcoiaicomtat.backend.server.base.ApplicationTestBase
import com.arnyminerz.escalaralcoiaicomtat.backend.server.error.Errors
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestFileFetching : ApplicationTestBase() {
    @Test
    fun `test data`() = test {
        val areaId = DataProvider.provideSampleArea()
        assertNotNull(areaId)

        val area: Area = ServerDatabase.instance.query { Area[areaId] }

        get("/file/${area.image.name}").apply {
            assertSuccess { data ->
                assertNotNull(data)
                assertTrue(data.has("hash"))
                assertTrue(data.has("filename"))
                assertTrue(data.has("download"))
                assertTrue(data.has("size"))
            }
        }
    }

    @Test
    fun `test doesn't exist`() = test {
        get("/file/unknown").apply {
            assertFailure(Errors.FileNotFound)
        }
    }
}
