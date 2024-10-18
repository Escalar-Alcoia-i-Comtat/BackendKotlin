package database.entity.info

import ServerDatabase.Companion.VERSION

object Version: InfoEntryCompanion<Int> {
    private const val ID = "version"

    override fun get(): Int? = InfoEntry.findById(ID)?.value?.toInt()

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

    fun set(value: Int) = update(value)

    fun updateRequired(): Boolean = get() != VERSION
}
