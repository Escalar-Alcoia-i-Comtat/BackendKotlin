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

suspend fun EntityTypes<*>.provide(builder: StubApplicationTestBuilder): Int? {
    when (this) {
        EntityTypes.AREA -> {
            return with(DataProvider) { builder.provideSampleArea() }
        }
        EntityTypes.ZONE -> {
            val areaId = with(DataProvider) { builder.provideSampleArea() }
            assertNotNull(areaId)
            return with(DataProvider) { builder.provideSampleZone(areaId) }
        }
        EntityTypes.SECTOR -> {
            val areaId = with(DataProvider) { builder.provideSampleArea() }
            assertNotNull(areaId)

            val zoneId = with(DataProvider) { builder.provideSampleZone(areaId) }
            assertNotNull(zoneId)

            return with(DataProvider) { builder.provideSampleSector(zoneId) }
        }
        EntityTypes.PATH -> {
            val areaId = with(DataProvider) { builder.provideSampleArea() }
            assertNotNull(areaId)

            val zoneId = with(DataProvider) { builder.provideSampleZone(areaId) }
            assertNotNull(zoneId)

            val sectorId = with(DataProvider) { builder.provideSampleSector(zoneId) }
            assertNotNull(sectorId)

            return with(DataProvider) { builder.provideSamplePath(sectorId) }
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
