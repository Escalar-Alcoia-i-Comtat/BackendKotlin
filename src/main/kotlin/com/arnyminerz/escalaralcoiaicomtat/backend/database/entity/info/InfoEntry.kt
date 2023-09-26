package com.arnyminerz.escalaralcoiaicomtat.backend.database.entity.info

import com.arnyminerz.escalaralcoiaicomtat.backend.database.table.InfoTable
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.id.EntityID

abstract class InfoEntry(id: String): Entity<String>(EntityID(id = id, table = InfoTable)) {
    var value by InfoTable.value
}
