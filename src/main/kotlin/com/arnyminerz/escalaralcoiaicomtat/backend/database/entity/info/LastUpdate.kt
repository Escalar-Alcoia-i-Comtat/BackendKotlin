package com.arnyminerz.escalaralcoiaicomtat.backend.database.entity.info

import com.arnyminerz.escalaralcoiaicomtat.backend.database.table.InfoTable
import java.time.Instant
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID

class LastUpdate(id: EntityID<String>): InfoEntry(id) {

    companion object: EntityClass<String, LastUpdate>(InfoTable), InfoEntryCompanion<Instant> {
        private const val ID = "last_update"

        override fun get(): Instant? = findById(ID)?.value?.toLong()?.let(Instant::ofEpochMilli)

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

        fun set(value: Instant = Instant.now()) = update(value)
    }
}
