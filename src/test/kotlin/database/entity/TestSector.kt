package database.entity

import ServerDatabase
import database.entity.DatabaseHelper.createTestArea
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

class TestSector: ApplicationTestBase() {
    @Test
    fun `test Sector_equals`() = test {
        val area = createTestArea()
        val zone = createTestZone(area)

        val sector1 = createTestSector(zone)
        val sector2 = createTestSector(zone)
        val sector3 = ServerDatabase.instance.query { Sector.findById(sector1.id) }

        assertFalse(sector1.equals(""))
        assertNotEquals(sector1, sector2)
        assertEquals(sector1, sector3)
    }

    @Test
    fun `test Sector serialization`() = test {
        val area = createTestArea()
        val zone = createTestZone(area)
        val sector = createTestSector(zone)

        val json = Json.encodeToJsonElement(sector).jsonObject
        assertTrue(json.containsKey("id"))
        assertTrue(json.containsKey("timestamp"))
        assertTrue(json.containsKey("display_name"))
        assertTrue(json.containsKey("kids_apt"))
        assertTrue(json.containsKey("sun_time"))
        assertTrue(json.containsKey("walking_time"))
        assertTrue(json.containsKey("image"))
        assertTrue(json.containsKey("gpx"))
        assertTrue(json.containsKey("point"))
        assertTrue(json.containsKey("zone_id"))
    }
}
