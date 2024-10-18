package database.entity.info

import java.time.Instant

object LastUpdate : InfoEntryCompanion<Instant> {
    private const val ID = "last_update"

    override fun get(): Instant? = InfoEntry.findById(ID)?.value?.toLong()?.let(Instant::ofEpochMilli)

    override fun update(value: Instant) {
        val entry = InfoEntry.findById(ID)
        if (entry != null) {
            // Already exists, update
            entry.value = value.toEpochMilli().toString()
        } else {
            InfoEntry.new(ID) {
                this.value = value.toEpochMilli().toString()
            }
        }
    }

    fun set(value: Instant = Instant.now()) = update(value)
}
