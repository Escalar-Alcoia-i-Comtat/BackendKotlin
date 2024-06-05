package database.entity

import ServerDatabase
import database.entity.DatabaseHelper.createTestArea
import database.entity.DatabaseHelper.createTestZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
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
}
