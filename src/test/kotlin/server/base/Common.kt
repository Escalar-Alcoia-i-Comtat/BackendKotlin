package server.base

import database.EntityTypes
import database.entity.Area
import database.entity.BaseEntity
import database.entity.Blocking
import database.entity.Path
import database.entity.Sector
import database.entity.Zone
import kotlin.test.assertNotNull
import server.DataProvider

context(StubApplicationTestBuilder)
suspend fun EntityTypes<*>.provide(): Int? {
    when (this) {
        EntityTypes.AREA -> {
            return DataProvider.provideSampleArea()
        }
        EntityTypes.ZONE -> {
            val areaId = DataProvider.provideSampleArea()
            assertNotNull(areaId)
            return DataProvider.provideSampleZone(areaId)
        }
        EntityTypes.SECTOR -> {
            val areaId = DataProvider.provideSampleArea()
            assertNotNull(areaId)

            val zoneId = DataProvider.provideSampleZone(areaId)
            assertNotNull(zoneId)

            return DataProvider.provideSampleSector(zoneId)
        }
        EntityTypes.PATH -> {
            val areaId = DataProvider.provideSampleArea()
            assertNotNull(areaId)

            val zoneId = DataProvider.provideSampleZone(areaId)
            assertNotNull(zoneId)

            val sectorId = DataProvider.provideSampleSector(zoneId)
            assertNotNull(sectorId)

            return DataProvider.provideSamplePath(sectorId)
        }
        else -> error("Not implemented")
    }
}

@Suppress("UNCHECKED_CAST")
fun <Type: BaseEntity> EntityTypes<Type>.getter(id: Int): Type {
    return when (this) {
        EntityTypes.AREA -> Area[id] as Type
        EntityTypes.ZONE -> Zone[id] as Type
        EntityTypes.SECTOR -> Sector[id] as Type
        EntityTypes.PATH -> Path[id] as Type
        EntityTypes.BLOCKING -> Blocking[id] as Type
        else -> error("Not implemented")
    }
}
