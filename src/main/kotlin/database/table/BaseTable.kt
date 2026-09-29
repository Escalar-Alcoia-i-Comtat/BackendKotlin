package database.table

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.javatime.CurrentTimestamp
import org.jetbrains.exposed.v1.javatime.timestamp

abstract class BaseTable: IntIdTable() {
    val timestamp = timestamp("timestamp").defaultExpression(CurrentTimestamp)
}
