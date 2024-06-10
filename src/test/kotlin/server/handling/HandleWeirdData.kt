package server.handling

import ServerDatabase
import assertions.assertSuccess
import database.entity.Path
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import server.DataProvider
import server.base.ApplicationTestBase

/**
 * This class handles states where the data stored in the database for a given item may be old or not well formatted.
 */
class HandleWeirdData: ApplicationTestBase() {
    @Test
    fun `test Path - empty pitches`() = test {
        val areaId = DataProvider.provideSampleArea()
        val zoneId = DataProvider.provideSampleZone(areaId)
        val sectorId = DataProvider.provideSampleSector(zoneId)
        val pathId = DataProvider.provideSamplePath(sectorId)

        assertNotNull(pathId)

        ServerDatabase.instance.query {
            val path = Path.findById(pathId)
            assertNotNull(path)

            path._pitches = ""
        }

        get("/path/$pathId").apply {
            assertSuccess<Path> { data ->
                assertNotNull(data)
                assertNull(data.pitches)
            }
        }
    }
}
