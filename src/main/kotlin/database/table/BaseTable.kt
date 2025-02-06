package database.table

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp

abstract class BaseTable: IntIdTable() {
    val timestamp = timestamp("timestamp").defaultExpression(CurrentTimestamp)
}
