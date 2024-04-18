package database

import database.entity.Area
import database.entity.BaseEntity
import database.entity.Blocking
import database.entity.Path
import database.entity.Sector
import database.entity.Zone

sealed class EntityTypes<Entity: BaseEntity>(
    /**
     * The name used for identifying the entity in the url.
     * For example, areas use `area`, since the url for accessing them is `/area/{id}`.
     */
    val urlName: String
) {
    data object AREA: EntityTypes<Area>("area")
    data object ZONE: EntityTypes<Zone>("zone")
    data object SECTOR: EntityTypes<Sector>("sector")
    data object PATH: EntityTypes<Path>("path")
    data object BLOCKING: EntityTypes<Blocking>("blocking")

    val name = this::class.simpleName
}
