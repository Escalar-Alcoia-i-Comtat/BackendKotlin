package com.arnyminerz.escalaralcoiaicomtat.backend.server.handling

import com.arnyminerz.escalaralcoiaicomtat.backend.ServerDatabase
import com.arnyminerz.escalaralcoiaicomtat.backend.assertions.assertSuccess
import com.arnyminerz.escalaralcoiaicomtat.backend.database.entity.Path
import com.arnyminerz.escalaralcoiaicomtat.backend.server.DataProvider
import com.arnyminerz.escalaralcoiaicomtat.backend.server.base.ApplicationTestBase
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.getJSONArrayOrNull
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

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
            assertSuccess { data ->
                assertNotNull(data)
                assertNull(data.getJSONArrayOrNull("pitches"))
            }
        }
    }
}
