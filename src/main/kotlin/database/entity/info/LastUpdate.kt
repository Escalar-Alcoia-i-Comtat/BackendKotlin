package database.entity.info

import database.table.InfoTable
import java.time.Instant
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Transaction

class LastUpdate(id: EntityID<String>): InfoEntry(id) {

    companion object: EntityClass<String, LastUpdate>(InfoTable), InfoEntryCompanion<Instant> {
        private const val ID = "last_update"

        context(Transaction)
        override fun get(): Instant? = findById(ID)?.value?.toLong()?.let(Instant::ofEpochMilli)

        context(Transaction)
        override fun update(value: Instant) {
            val entry = findById(ID)
            if (entry != null) {
                // Already exists, update
                entry.value = value.toEpochMilli().toString()
            } else {
                LastUpdate.new(ID) {
                    this.value = value.toEpochMilli().toString()
                }
            }
        }

        context(Transaction)
        fun set(value: Instant = Instant.now()) = update(value)
    }
}
