package database.entity.info

import database.table.InfoTable
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.id.EntityID

abstract class InfoEntry(id: EntityID<String>): Entity<String>(id) {
    var value by InfoTable.value
}
