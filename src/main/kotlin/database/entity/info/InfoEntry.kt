package database.entity.info

import database.table.InfoTable
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID

class InfoEntry(id: EntityID<String>): Entity<String>(id) {
    companion object: EntityClass<String, InfoEntry>(InfoTable)

    var value by InfoTable.value
}
