package server.endpoints.query

import assertions.assertSuccessRaw
import database.serialization.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import server.DataProvider
import server.base.ApplicationTestBase

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
            assertSuccessRaw { data ->
                val json = Json.decodeFromString<JsonElement>(data).jsonObject
                val data = json["data"]?.jsonObject
                assertNotNull(data)

                val areas = data["areas"]?.jsonArray
                assertNotNull(areas)
                assertEquals(1, areas.size)

                val area = areas[0].jsonObject
                val zones = area["zones"]?.jsonArray

                assertNotNull(zones)
                assertEquals(1, zones.size)

                val zone = zones[0].jsonObject
                val sectors = zone["sectors"]?.jsonArray
                assertNotNull(sectors)
                assertEquals(1, sectors.size)

                val sector = sectors[0].jsonObject
                val paths = sector["paths"]?.jsonArray
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
            assertSuccessRaw { data ->
                val json = Json.decodeFromString<JsonElement>(data).jsonObject
                val data = json["data"]?.jsonObject
                assertNotNull(data)

                val areas = data["areas"]?.jsonArray
                assertNotNull(areas)
                assertEquals(1, areas.size)

                val area = areas[0].jsonObject
                val zones = area["zones"]?.jsonArray
                assertNotNull(zones)
                assertEquals(1, zones.size)
            }
        }
    }
}
