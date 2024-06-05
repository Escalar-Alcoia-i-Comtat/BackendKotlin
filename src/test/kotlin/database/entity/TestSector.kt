package database.entity

import ServerDatabase
import database.entity.DatabaseHelper.createTestArea
import database.entity.DatabaseHelper.createTestSector
import database.entity.DatabaseHelper.createTestZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
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
}
