package server.endpoints.query

import assertions.assertSuccess
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import server.DataProvider
import server.base.ApplicationTestBase
import server.response.query.TreeResponseData

class TestTreeEndpoint : ApplicationTestBase() {
    @Test
    fun `test getting tree`() = test {
        val areaId = DataProvider.provideSampleArea(this)
        assertNotNull(areaId)

        val zoneId = DataProvider.provideSampleZone(this, areaId)
        assertNotNull(zoneId)

        val sectorId = DataProvider.provideSampleSector(this, zoneId)
        assertNotNull(sectorId)

        val pathId = DataProvider.provideSamplePath(this, sectorId)
        assertNotNull(pathId)

        get("/tree").apply {
            assertSuccess<TreeResponseData> { data ->
                assertNotNull(data)

                val areas = data.areas
                assertEquals(1, areas.size)

                val area = areas[0]
                val zones = area.zones

                assertNotNull(zones)
                assertEquals(1, zones.size)

                val zone = zones[0]
                val sectors = zone.sectors
                assertNotNull(sectors)
                assertEquals(1, sectors.size)

                val sector = sectors[0]
                val paths = sector.paths
                assertNotNull(paths)
                assertEquals(1, paths.size)
            }
        }
    }

    @Test
    fun `test getting tree - zone without points`() = test {
        val areaId = DataProvider.provideSampleArea(this)
        assertNotNull(areaId)

        val zoneId = DataProvider.provideSampleZone(this, areaId, emptyPoints = true)
        assertNotNull(zoneId)

        get("/tree").apply {
            assertSuccess<TreeResponseData> { data ->
                assertNotNull(data)

                val areas = data.areas
                assertNotNull(areas)
                assertEquals(1, areas.size)
                val area = areas[0]

                val zones = area.zones
                assertNotNull(zones)
                assertEquals(1, zones.size)
            }
        }
    }
}
