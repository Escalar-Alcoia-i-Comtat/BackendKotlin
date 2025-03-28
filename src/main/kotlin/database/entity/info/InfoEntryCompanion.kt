package database.entity.info

interface InfoEntryCompanion<Type: Any> {
    fun get(): Type?

    fun update(value: Type)

    fun isInitialized(): Boolean = get() != null
}
