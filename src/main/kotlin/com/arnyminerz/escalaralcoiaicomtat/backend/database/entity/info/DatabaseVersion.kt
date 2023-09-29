package com.arnyminerz.escalaralcoiaicomtat.backend.database.entity.info

import com.arnyminerz.escalaralcoiaicomtat.backend.database.table.InfoTable
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Transaction

class DatabaseVersion(id: EntityID<String>): InfoEntry(id) {
    companion object: EntityClass<String, LastUpdate>(InfoTable), InfoEntryCompanion<Int> {
        private const val ID = "database_version"

        context(Transaction)
        override fun get(): Int? = findById(ID)?.value?.toIntOrNull()

        context(Transaction)
        override fun update(value: Int) {
            val entry = findById(ID)
            if (entry != null) {
                // Already exists, update
                entry.value = value.toString()
            } else {
                new(ID) {
                    this.value = value.toString()
                }
            }
        }
    }
}
