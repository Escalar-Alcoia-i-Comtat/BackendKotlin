package database.entity.info

import database.table.InfoTable
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.id.EntityID

abstract class InfoEntry(id: EntityID<String>): Entity<String>(id) {
    constructor(id: String): this(EntityID(id = id, table = InfoTable))

    var value by InfoTable.value
}
