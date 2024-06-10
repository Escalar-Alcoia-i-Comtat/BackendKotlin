package database.entity

import ServerDatabase
import database.entity.DatabaseHelper.createTestArea
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import server.base.ApplicationTestBase

class TestArea: ApplicationTestBase() {
    @Test
    fun `test Area_equals`() = test {
        val area1 = createTestArea()
        val area2 = createTestArea()
        val area3 = ServerDatabase.instance.query { Area.findById(area1.id) }

        assertFalse(area1.equals(""))
        assertNotEquals(area1, area2)
        assertEquals(area1, area3)
    }

    @Test
    fun `test Area serialization`() = test {
        val area = createTestArea()
        val json = Json.encodeToJsonElement(area).jsonObject
        assertTrue(json.containsKey("id"))
        assertTrue(json.containsKey("timestamp"))
        assertTrue(json.containsKey("display_name"))
        assertTrue(json.containsKey("web_url"))
        assertTrue(json.containsKey("image"))
    }
}
