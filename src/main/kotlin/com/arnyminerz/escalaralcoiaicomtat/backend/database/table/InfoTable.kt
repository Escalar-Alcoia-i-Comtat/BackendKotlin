package com.arnyminerz.escalaralcoiaicomtat.backend.database.table

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column

object InfoTable: IdTable<String>() {
    override val id: Column<EntityID<String>> = varchar("id", 16).entityId()
    val value: Column<String> = varchar("value", 128)

    override val primaryKey: PrimaryKey = PrimaryKey(id, name = "PK_InfoTable_Name")
}
