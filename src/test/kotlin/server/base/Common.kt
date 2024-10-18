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
            return DataProvider.provideSampleArea(builder)
        }

        EntityTypes.ZONE -> {
            val areaId = DataProvider.provideSampleArea(builder)
            assertNotNull(areaId)
            return DataProvider.provideSampleZone(builder, areaId)
        }

        EntityTypes.SECTOR -> {
            val areaId = DataProvider.provideSampleArea(builder)
            assertNotNull(areaId)

            val zoneId = DataProvider.provideSampleZone(builder, areaId)
            assertNotNull(zoneId)

            return DataProvider.provideSampleSector(builder, zoneId)
        }

        EntityTypes.PATH -> {
            val areaId = DataProvider.provideSampleArea(builder)
            assertNotNull(areaId)

            val zoneId = DataProvider.provideSampleZone(builder, areaId)
            assertNotNull(zoneId)

            val sectorId = DataProvider.provideSampleSector(builder, zoneId)
            assertNotNull(sectorId)

            return DataProvider.provideSamplePath(builder, sectorId)
        }

        else -> error("Not implemented")
    }
}

@Suppress("UNCHECKED_CAST")
fun <Type : BaseEntity> EntityTypes<Type>.getter(id: Int): Type {
    return when (this) {
        EntityTypes.AREA -> Area[id] as Type
        EntityTypes.ZONE -> Zone[id] as Type
        EntityTypes.SECTOR -> Sector[id] as Type
        EntityTypes.PATH -> Path[id] as Type
        EntityTypes.BLOCKING -> Blocking[id] as Type
        else -> error("Not implemented")
    }
}
