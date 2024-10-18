package database.entity.info

import org.jetbrains.exposed.sql.Transaction

interface InfoEntryCompanion<Type: Any> {
    fun Transaction.get(): Type?

    fun Transaction.update(value: Type)
}
