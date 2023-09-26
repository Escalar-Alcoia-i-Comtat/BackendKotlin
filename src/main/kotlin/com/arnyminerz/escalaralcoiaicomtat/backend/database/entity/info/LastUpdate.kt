package com.arnyminerz.escalaralcoiaicomtat.backend.database.entity.info

import com.arnyminerz.escalaralcoiaicomtat.backend.database.table.InfoTable
import java.time.Instant
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID

class LastUpdate(id: EntityID<String>): InfoEntry(id) {

    companion object: EntityClass<String, LastUpdate>(InfoTable) {
        private const val ID = "last_update"

        fun get(): Instant? = findById(ID)?.value?.toLong()?.let(Instant::ofEpochMilli)

        fun set(timestamp: Instant = Instant.now()) {
            val entry = findById(ID)
            if (entry != null) {
                // Already exists, update
                entry.value = timestamp.toEpochMilli().toString()
            } else {
                LastUpdate.new(ID) {
                    value = timestamp.toEpochMilli().toString()
                }
            }
        }
    }
}
