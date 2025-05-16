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

suspend fun EntityTypes<*>.provide(
    builder: StubApplicationTestBuilder,
    provideParent: (suspend () -> Int?)? = null,
    provideChildren: (suspend (parentId: Int) -> Int?)? = null,
): Int? {
    return when (this) {
        EntityTypes.AREA -> {
            DataProvider.provideSampleArea(builder)
        }

        EntityTypes.ZONE -> {
            val areaId = provideParent?.invoke() ?: DataProvider.provideSampleArea(builder)
            assertNotNull(areaId)

            DataProvider.provideSampleZone(builder, areaId)
        }

        EntityTypes.SECTOR -> {
            val zoneId = provideParent?.invoke() ?: run {
                val areaId = DataProvider.provideSampleArea(builder)
                assertNotNull(areaId)

                DataProvider.provideSampleZone(builder, areaId)
            }
            assertNotNull(zoneId)

            DataProvider.provideSampleSector(builder, zoneId)
        }

        EntityTypes.PATH -> {
            val sectorId = provideParent?.invoke() ?: run {
                val areaId = DataProvider.provideSampleArea(builder)
                assertNotNull(areaId)

                val zoneId = DataProvider.provideSampleZone(builder, areaId)
                assertNotNull(zoneId)

                DataProvider.provideSampleSector(builder, zoneId)
            }
            assertNotNull(sectorId)

            DataProvider.provideSamplePath(builder, sectorId)
        }

        else -> error("Not implemented")
    }.also { id -> id?.let { provideChildren?.invoke(it) } }
}

@Suppress("UNCHECKED_CAST")
fun <Type : BaseEntity> EntityTypes<Type>.getter(id: Int): Type {
    return when (this) {
        EntityTypes.AREA -> Area[id] as Type
        EntityTypes.ZONE -> Zone[id] as Type
        EntityTypes.SECTOR -> Sector[id] as Type
        EntityTypes.PATH -> Path[id] as Type
        EntityTypes.BLOCKING -> Blocking[id] as Type
    }
}
