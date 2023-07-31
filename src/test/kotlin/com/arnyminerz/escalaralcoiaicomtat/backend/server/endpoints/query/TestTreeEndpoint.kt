package com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.query

import com.arnyminerz.escalaralcoiaicomtat.backend.assertions.assertSuccess
import com.arnyminerz.escalaralcoiaicomtat.backend.server.base.ApplicationTestBase
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.DataProvider
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestTreeEndpoint: ApplicationTestBase() {
    @Test
    fun `test getting tree`() = test {
        val areaId = DataProvider.provideSampleArea()
        assertNotNull(areaId)

        val zoneId = DataProvider.provideSampleZone(areaId)
        assertNotNull(zoneId)

        val sectorId = DataProvider.provideSampleSector(zoneId)
        assertNotNull(sectorId)

        val pathId = DataProvider.provideSamplePath(sectorId)
        assertNotNull(pathId)

        get("/tree").apply {
            assertSuccess { data ->
                assertNotNull(data)

                val areas = data.getJSONArray("areas")
                assertEquals(1, areas.length())

                val area = areas.getJSONObject(0)
                assertTrue(area.has("zones"))

                val zones = area.getJSONArray("zones")
                assertEquals(1, zones.length())

                val zone = zones.getJSONObject(0)
                assertTrue(zone.has("sectors"))

                val sectors = zone.getJSONArray("sectors")
                assertEquals(1, sectors.length())

                val sector = sectors.getJSONObject(0)
                assertTrue(sector.has("paths"))

                val paths = sector.getJSONArray("paths")
                assertEquals(1, paths.length())
            }
        }
    }

    @Test
    fun `test getting tree - zone without points`() = test {
        val areaId = DataProvider.provideSampleArea()
        assertNotNull(areaId)

        val zoneId = DataProvider.provideSampleZone(areaId, emptyPoints = true)
        assertNotNull(zoneId)

        get("/tree").apply {
            assertSuccess { data ->
                assertNotNull(data)

                assertTrue(data.has("areas"))
                val areas = data.getJSONArray("areas")
                assertEquals(1, areas.length())
                val area = areas.getJSONObject(0)

                assertTrue(area.has("zones"))
                val zones = data.getJSONArray("zones")
                assertEquals(1, zones.length())
            }
        }
    }
}
