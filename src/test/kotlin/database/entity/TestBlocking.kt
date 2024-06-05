package database.entity

import ServerDatabase
import database.entity.DatabaseHelper.createTestArea
import database.entity.DatabaseHelper.createTestBlocking
import database.entity.DatabaseHelper.createTestPath
import database.entity.DatabaseHelper.createTestSector
import database.entity.DatabaseHelper.createTestZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import server.DataProvider
import server.base.ApplicationTestBase

class TestBlocking: ApplicationTestBase() {
    @Test
    fun `test set recurrence and endDate`() = test {
        val area = createTestArea()
        val zone = createTestZone(area)
        val sector = createTestSector(zone)
        val path = createTestPath(sector)

        // Check that initially it has the correct recurrence, and endDate is null
        val blocking = createTestBlocking(path)
        ServerDatabase.instance.query {
            assertEquals(DataProvider.SampleBlocking.recurrence, blocking.recurrence)
            assertNull(blocking.endDate)
        }

        // Modify endDate. Recurrence will be voided
        ServerDatabase.instance.query {
            blocking.endDate = DataProvider.SampleBlocking.endDate
        }

        // Perform checks
        ServerDatabase.instance.query {
            assertEquals(DataProvider.SampleBlocking.endDate, blocking.endDate)
            assertNull(blocking.recurrence)
        }
    }
}
