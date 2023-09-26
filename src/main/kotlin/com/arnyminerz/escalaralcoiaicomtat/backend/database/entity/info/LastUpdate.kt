package com.arnyminerz.escalaralcoiaicomtat.backend.database.entity.info

import com.arnyminerz.escalaralcoiaicomtat.backend.database.table.InfoTable
import java.time.Instant
import org.jetbrains.exposed.dao.EntityClass

class LastUpdate: InfoEntry(ID) {
    companion object: EntityClass<String, LastUpdate>(InfoTable) {
        const val ID = "last_update"

        fun get(): Instant? = findById(ID)?.value?.toLong()?.let(Instant::ofEpochMilli)

        fun set(timestamp: Instant = Instant.now()) {
            val value = findById(ID)
            if (value != null) {
                // Already exists, update
                value.value = timestamp.toEpochMilli().toString()
            } else {
                LastUpdate.new {
                    this.value = timestamp.toEpochMilli().toString()
                }
            }
        }
    }
}
