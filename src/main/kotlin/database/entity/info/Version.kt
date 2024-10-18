package database.entity.info

import ServerDatabase.Companion.VERSION
import org.jetbrains.exposed.sql.Transaction

object Version: InfoEntryCompanion<Int> {
    private const val ID = "version"

    override fun Transaction.get(): Int? = InfoEntry.findById(ID)?.value?.toInt()

    override fun Transaction.update(value: Int) {
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

    fun Transaction.set(value: Int) = update(value)

    fun Transaction.updateRequired(): Boolean = get() != VERSION
}
