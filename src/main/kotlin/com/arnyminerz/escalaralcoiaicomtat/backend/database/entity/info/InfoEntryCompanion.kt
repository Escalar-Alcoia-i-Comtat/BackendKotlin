package com.arnyminerz.escalaralcoiaicomtat.backend.database.entity.info

interface InfoEntryCompanion<Type: Any> {
    fun get(): Type?

    fun update(value: Type)
}
