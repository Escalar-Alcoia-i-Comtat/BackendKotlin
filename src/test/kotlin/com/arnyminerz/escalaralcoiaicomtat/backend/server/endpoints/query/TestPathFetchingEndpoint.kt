package com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.query

import com.arnyminerz.escalaralcoiaicomtat.backend.ServerDatabase
import com.arnyminerz.escalaralcoiaicomtat.backend.assertions.assertFailure
import com.arnyminerz.escalaralcoiaicomtat.backend.assertions.assertSuccess
import com.arnyminerz.escalaralcoiaicomtat.backend.data.Builder
import com.arnyminerz.escalaralcoiaicomtat.backend.data.Ending
import com.arnyminerz.escalaralcoiaicomtat.backend.data.GradeValue
import com.arnyminerz.escalaralcoiaicomtat.backend.data.PitchInfo
import com.arnyminerz.escalaralcoiaicomtat.backend.database.entity.Path
import com.arnyminerz.escalaralcoiaicomtat.backend.server.DataProvider
import com.arnyminerz.escalaralcoiaicomtat.backend.server.base.ApplicationTestBase
import com.arnyminerz.escalaralcoiaicomtat.backend.server.error.Errors
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.getLongOrNull
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.getStringOrNull
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.getUInt
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.serialize
import io.ktor.http.HttpStatusCode
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TestPathFetchingEndpoint: ApplicationTestBase() {
    @Test
    fun `test getting path`() = test {
        val areaId = DataProvider.provideSampleArea()
        assertNotNull(areaId)

        val zoneId = DataProvider.provideSampleZone(areaId)
        assertNotNull(zoneId)

        val sectorId = DataProvider.provideSampleSector(zoneId)
        assertNotNull(sectorId)

        val pathId = DataProvider.provideSamplePath(sectorId)
        assertNotNull(pathId)

        get("/path/$pathId").apply {
            assertSuccess { data ->
                assertNotNull(data)

                println(data.toString(2))

                assertEquals(zoneId, data.getInt("id"))
                assertEquals(sectorId, data.getInt("sector_id"))

                assertEquals(DataProvider.SamplePath.displayName, data.getString("display_name"))
                assertEquals(DataProvider.SamplePath.sketchId, data.getUInt("sketch_id"))

                assertEquals(DataProvider.SamplePath.height, data.getUInt("height"))
                assertEquals(DataProvider.SamplePath.grade, data.getString("grade").let { GradeValue.fromString(it) })
                assertEquals(DataProvider.SamplePath.ending, data.getString("ending").let { Ending.valueOf(it) })

                assertContentEquals(DataProvider.SamplePath.pitches, data.getJSONArray("pitches").serialize(PitchInfo))

                assertEquals(DataProvider.SamplePath.stringCount, data.getUInt("string_count"))

                assertEquals(DataProvider.SamplePath.paraboltCount, data.getUInt("parabolt_count"))
                assertEquals(DataProvider.SamplePath.burilCount, data.getUInt("buril_count"))
                assertEquals(DataProvider.SamplePath.pitonCount, data.getUInt("piton_count"))
                assertEquals(DataProvider.SamplePath.spitCount, data.getUInt("spit_count"))
                assertEquals(DataProvider.SamplePath.tensorCount, data.getUInt("tensor_count"))

                assertEquals(DataProvider.SamplePath.crackerRequired, data.getBoolean("cracker_required"))
                assertEquals(DataProvider.SamplePath.friendRequired, data.getBoolean("friend_required"))
                assertEquals(DataProvider.SamplePath.lanyardRequired, data.getBoolean("lanyard_required"))
                assertEquals(DataProvider.SamplePath.nailRequired, data.getBoolean("nail_required"))
                assertEquals(DataProvider.SamplePath.pitonRequired, data.getBoolean("piton_required"))
                assertEquals(DataProvider.SamplePath.stapesRequired, data.getBoolean("stapes_required"))

                assertEquals(DataProvider.SamplePath.builder, data.getJSONObject("builder").let(Builder::fromJson))
                assertContentEquals(
                    DataProvider.SamplePath.reBuilder,
                    data.getJSONArray("re_builder").serialize(Builder)
                )

                assertTrue(data.getLong("timestamp") < Instant.now().toEpochMilli())
            }
        }
    }

    @Test
    fun `test getting path - with image`() = test {
        val areaId = DataProvider.provideSampleArea()
        assertNotNull(areaId)

        val zoneId = DataProvider.provideSampleZone(areaId)
        assertNotNull(zoneId)

        val sectorId = DataProvider.provideSampleSector(zoneId)
        assertNotNull(sectorId)

        val pathId = DataProvider.provideSamplePath(sectorId, images = listOf("/images/uixola.jpg"))
        assertNotNull(pathId)

        var image: String? = null

        get("/path/$pathId").apply {
            assertSuccess { data ->
                assertNotNull(data)

                val images = data.getJSONArray("images")
                assertEquals(1, images.length())
                image = images.getString(0)
            }
        }

        assertNotNull(image)

        get("/file/$image").apply {
            assertSuccess { data ->
                assertNotNull(data?.getStringOrNull("download"))
                assertNotNull(data?.getStringOrNull("filename"))
                assertNotNull(data?.getStringOrNull("hash"))
                assertNotNull(data?.getLongOrNull("size"))
            }
        }
    }

    @Test
    fun `test getting path - doesn't exist`() = test {
        get("/path/123").apply {
            assertFailure(Errors.ObjectNotFound)
        }
    }

    @Test
    fun `test getting path - id NaN`() = test {
        get("/path/abc").apply {
            assertEquals(HttpStatusCode.BadRequest, status)
        }
    }

    @Test
    fun `test getting path - fix null builder`() = test {
        val areaId = DataProvider.provideSampleArea()
        assertNotNull(areaId)

        val zoneId = DataProvider.provideSampleZone(areaId)
        assertNotNull(zoneId)

        val sectorId = DataProvider.provideSampleSector(zoneId)
        assertNotNull(sectorId)

        val pathId = DataProvider.provideSamplePath(sectorId)
        assertNotNull(pathId)

        ServerDatabase.instance.query {
            Path[pathId]._builder = "null"
        }

        get("/path/$pathId").apply {
            assertSuccess()
        }

        ServerDatabase.instance.query {
            assertNull(Path[pathId].builder)
        }
    }

    @Test
    fun `test getting path - fix invalid pitches`() = test {
        val areaId = DataProvider.provideSampleArea()
        assertNotNull(areaId)

        val zoneId = DataProvider.provideSampleZone(areaId)
        assertNotNull(zoneId)

        val sectorId = DataProvider.provideSampleSector(zoneId)
        assertNotNull(sectorId)

        val pathId = DataProvider.provideSamplePath(sectorId)
        assertNotNull(pathId)

        ServerDatabase.instance.query {
            Path[pathId]._pitches = "{\"pitch\":\"0\"}"
        }

        get("/path/$pathId").apply {
            assertSuccess()
        }

        ServerDatabase.instance.query {
            assertNull(Path[pathId].pitches)
        }
    }
}
