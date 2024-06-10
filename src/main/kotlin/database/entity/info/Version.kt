package database.entity.info

import database.table.InfoTable
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Transaction

class Version(id: EntityID<String>): InfoEntry(id) {
    companion object: EntityClass<String, Version>(InfoTable), InfoEntryCompanion<Int> {
        private const val ID = "version"

        context(Transaction)
        override fun get(): Int? = findById(ID)?.value?.toInt()

        context(Transaction)
        override fun update(value: Int) {
            val entry = findById(ID)
            if (entry != null) {
                // Already exists, update
                entry.value = value.toString()
            } else {
                LastUpdate.new(ID) {
                    this.value = value.toString()
                }
            }
        }

        context(Transaction)
        fun set(value: Int) = update(value)
    }
}
