package com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.files

import com.arnyminerz.escalaralcoiaicomtat.backend.ServerDatabase
import com.arnyminerz.escalaralcoiaicomtat.backend.assertions.assertFailure
import com.arnyminerz.escalaralcoiaicomtat.backend.assertions.assertSuccess
import com.arnyminerz.escalaralcoiaicomtat.backend.database.entity.Area
import com.arnyminerz.escalaralcoiaicomtat.backend.database.entity.Zone
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
                assertTrue(data.has("uuid"))
                assertTrue(data.has("hash"))
                assertTrue(data.has("filename"))
                assertTrue(data.has("download"))
                assertTrue(data.has("size"))
            }
        }
    }

    @Test
    fun `test data no extension`() = test {
        val areaId = DataProvider.provideSampleArea()
        assertNotNull(areaId)

        val area: Area = ServerDatabase.instance.query { Area[areaId] }

        println("Image: ${area.image.nameWithoutExtension}")
        get("/file/${area.image.nameWithoutExtension}").apply {
            assertSuccess { data ->
                assertNotNull(data)
                assertTrue(data.has("uuid"))
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

    @Test
    fun `test data multiple`() = test {
        val areaId = DataProvider.provideSampleArea()
        assertNotNull(areaId)
        val zoneId = DataProvider.provideSampleZone(areaId)
        assertNotNull(zoneId)

        val area: Area = ServerDatabase.instance.query { Area[areaId] }
        val zone: Zone = ServerDatabase.instance.query { Zone[zoneId] }

        get("/file/${area.image.name},${zone.image.name}").apply {
            assertSuccess { data ->
                assertNotNull(data)
                assertTrue(data.has("files"))

                val files = data.getJSONArray("files")
                (0 until files.length())
                    .map { files.getJSONObject(it) }
                    .forEach { file ->
                        assertTrue(file.has("uuid"))
                        assertTrue(file.has("hash"))
                        assertTrue(file.has("filename"))
                        assertTrue(file.has("download"))
                        assertTrue(file.has("size"))
                    }
            }
        }
    }
}
