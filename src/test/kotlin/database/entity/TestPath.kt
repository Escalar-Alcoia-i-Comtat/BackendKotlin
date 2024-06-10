package database.entity

import ServerDatabase
import database.entity.DatabaseHelper.createTestArea
import database.entity.DatabaseHelper.createTestPath
import database.entity.DatabaseHelper.createTestSector
import database.entity.DatabaseHelper.createTestZone
import database.serialization.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import server.base.ApplicationTestBase

class TestPath: ApplicationTestBase() {
    @Test
    fun `test Path_equals`() = test {
        val area = createTestArea()
        val zone = createTestZone(area)
        val sector = createTestSector(zone)

        val path1 = createTestPath(sector)
        val path2 = createTestPath(sector)
        val path3 = ServerDatabase.instance.query { Path.findById(path1.id) }

        assertFalse(path1.equals(""))
        assertNotEquals(path1, path2)
        assertEquals(path1, path3)
    }

    @Test
    fun `test Path serialization`() = test {
        val area = createTestArea()
        val zone = createTestZone(area)
        val sector = createTestSector(zone)
        val path = createTestPath(sector)

        val json = Json.encodeToJsonElement(path).jsonObject
        assertTrue(json.containsKey("id"))
        assertTrue(json.containsKey("timestamp"))
        assertTrue(json.containsKey("display_name"))
        assertTrue(json.containsKey("sketch_id"))
        assertTrue(json.containsKey("height"))
        assertTrue(json.containsKey("grade"))
        assertTrue(json.containsKey("ending"))
        assertTrue(json.containsKey("pitches"))
        assertTrue(json.containsKey("string_count"))
        assertTrue(json.containsKey("parabolt_count"))
        assertTrue(json.containsKey("buril_count"))
        assertTrue(json.containsKey("piton_count"))
        assertTrue(json.containsKey("spit_count"))
        assertTrue(json.containsKey("tensor_count"))
        assertTrue(json.containsKey("cracker_required"))
        assertTrue(json.containsKey("friend_required"))
        assertTrue(json.containsKey("lanyard_required"))
        assertTrue(json.containsKey("nail_required"))
        assertTrue(json.containsKey("piton_required"))
        assertTrue(json.containsKey("stapes_required"))
        assertTrue(json.containsKey("show_description"))
        assertTrue(json.containsKey("description"))
        assertTrue(json.containsKey("builder"))
        assertTrue(json.containsKey("re_builder"))
        assertTrue(json.containsKey("images"))
        assertTrue(json.containsKey("sector_id"))
    }
}
