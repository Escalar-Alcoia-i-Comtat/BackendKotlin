package com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.create

import com.arnyminerz.escalaralcoiaicomtat.backend.ServerDatabase
import com.arnyminerz.escalaralcoiaicomtat.backend.assertions.assertFailure
import com.arnyminerz.escalaralcoiaicomtat.backend.database.entity.Path
import com.arnyminerz.escalaralcoiaicomtat.backend.server.base.ApplicationTestBase
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.DataProvider
import com.arnyminerz.escalaralcoiaicomtat.backend.server.error.Errors
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TestPathCreationEndpoint: ApplicationTestBase() {
    @Test
    fun `test path creation`() = test {
        val areaId: Int? = DataProvider.provideSampleArea()
        assertNotNull(areaId)

        val zoneId: Int? = DataProvider.provideSampleZone(areaId)
        assertNotNull(zoneId)

        val sectorId: Int? = DataProvider.provideSampleSector(zoneId)
        assertNotNull(sectorId)

        val pathId: Int? = DataProvider.provideSamplePath(sectorId)
        assertNotNull(pathId)

        ServerDatabase.instance.query {
            val path = Path[pathId]
            assertNotNull(path)
            assertEquals(DataProvider.SamplePath.displayName, path.displayName)
            assertEquals(DataProvider.SamplePath.sketchId, path.sketchId)

            assertEquals(DataProvider.SamplePath.height, path.height)
            assertEquals(DataProvider.SamplePath.grade, path.grade)

            assertEquals(DataProvider.SamplePath.pitches, path.pitches)

            assertEquals(DataProvider.SamplePath.stringCount, path.stringCount)

            assertEquals(DataProvider.SamplePath.paraboltCount, path.paraboltCount)
            assertEquals(DataProvider.SamplePath.burilCount, path.burilCount)
            assertEquals(DataProvider.SamplePath.pitonCount, path.pitonCount)
            assertEquals(DataProvider.SamplePath.spitCount, path.spitCount)
            assertEquals(DataProvider.SamplePath.tensorCount, path.tensorCount)

            assertEquals(DataProvider.SamplePath.crackerRequired, path.crackerRequired)
            assertEquals(DataProvider.SamplePath.friendRequired, path.friendRequired)
            assertEquals(DataProvider.SamplePath.lanyardRequired, path.lanyardRequired)
            assertEquals(DataProvider.SamplePath.nailRequired, path.nailRequired)
            assertEquals(DataProvider.SamplePath.pitonRequired, path.pitonRequired)
            assertEquals(DataProvider.SamplePath.stapesRequired, path.stapesRequired)
        }
    }

    @Test
    fun `test path creation - missing arguments`() = test {
        val areaId = DataProvider.provideSampleArea()
        val zoneId: Int? = DataProvider.provideSampleZone(areaId)
        val sectorId: Int? = DataProvider.provideSampleSector(zoneId)
        DataProvider.provideSamplePath(sectorId, skipDisplayName = true) {
            assertFailure(Errors.MissingData)
            null
        }
        DataProvider.provideSamplePath(sectorId, skipSketchId = true) {
            assertFailure(Errors.MissingData)
            null
        }
    }
}
