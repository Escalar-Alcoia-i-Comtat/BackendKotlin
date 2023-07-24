package com.arnyminerz.escalaralcoiaicomtat.backend.database.table

import java.time.Instant
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp

abstract class BaseTable: IntIdTable() {
    val timestamp = timestamp("timestamp").default(Instant.now())
}
