package database.entity

import ServerDatabase
import database.entity.DatabaseHelper.createTestArea
import database.entity.DatabaseHelper.createTestPath
import database.entity.DatabaseHelper.createTestSector
import database.entity.DatabaseHelper.createTestZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
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
}
