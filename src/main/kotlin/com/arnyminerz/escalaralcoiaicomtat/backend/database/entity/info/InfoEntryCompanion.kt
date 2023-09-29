package com.arnyminerz.escalaralcoiaicomtat.backend.database.entity.info

import org.jetbrains.exposed.sql.Transaction

interface InfoEntryCompanion<Type: Any> {
    context(Transaction)
    fun get(): Type?

    context(Transaction)
    fun update(value: Type)
}
