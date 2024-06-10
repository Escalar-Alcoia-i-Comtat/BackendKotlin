package database.entity

import ServerDatabase
import database.entity.DatabaseHelper.createTestArea
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

class TestZone: ApplicationTestBase() {
    @Test
    fun `test Zone_equals`() = test {
        val area = createTestArea()

        val zone1 = createTestZone(area)
        val zone2 = createTestZone(area)
        val zone3 = ServerDatabase.instance.query { Zone.findById(zone1.id) }

        assertFalse(zone1.equals(""))
        assertNotEquals(zone1, zone2)
        assertEquals(zone1, zone3)
    }

    @Test
    fun `test Zone serialization`() = test {
        val area = createTestArea()
        val zone = createTestZone(area)

        val json = Json.encodeToJsonElement(zone).jsonObject
        assertTrue(json.containsKey("id"))
        assertTrue(json.containsKey("timestamp"))
        assertTrue(json.containsKey("display_name"))
        assertTrue(json.containsKey("image"))
        assertTrue(json.containsKey("kmz"))
        assertTrue(json.containsKey("web_url"))
        assertTrue(json.containsKey("point"))
        assertTrue(json.containsKey("points"))
        assertTrue(json.containsKey("area_id"))
    }
}
