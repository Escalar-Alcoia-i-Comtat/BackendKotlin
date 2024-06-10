package database.entity.info

import ServerDatabase.Companion.VERSION
import org.jetbrains.exposed.sql.Transaction

object Version: InfoEntryCompanion<Int> {
    private const val ID = "version"

    context(Transaction)
    override fun get(): Int? = InfoEntry.findById(ID)?.value?.toInt()

    context(Transaction)
    override fun update(value: Int) {
        val entry = InfoEntry.findById(ID)
        if (entry != null) {
            // Already exists, update
            entry.value = value.toString()
        } else {
            InfoEntry.new(ID) {
                this.value = value.toString()
            }
        }
    }

    context(Transaction)
    fun set(value: Int) = update(value)

    context(Transaction)
    fun updateRequired(): Boolean = get() != VERSION
}
