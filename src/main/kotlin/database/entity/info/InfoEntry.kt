package database.entity.info

import database.table.InfoTable
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass
import org.jetbrains.exposed.v1.core.dao.id.EntityID

class InfoEntry(id: EntityID<String>): Entity<String>(id) {
    companion object: EntityClass<String, InfoEntry>(InfoTable)

    var value by InfoTable.value
}
