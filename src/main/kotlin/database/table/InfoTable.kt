package database.table

import database.SqlConsts.INFO_ID_LENGTH
import database.SqlConsts.INFO_VALUE_LENGTH
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column

object InfoTable: IdTable<String>() {
    override val id: Column<EntityID<String>> = varchar("id", INFO_ID_LENGTH).entityId()
    val value: Column<String> = varchar("value", INFO_VALUE_LENGTH)

    override val primaryKey: PrimaryKey = PrimaryKey(id, name = "PK_InfoTable_Name")
}
