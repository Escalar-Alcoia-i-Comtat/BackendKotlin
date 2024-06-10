package database.entity.info

import java.time.Instant
import org.jetbrains.exposed.sql.Transaction

object LastUpdate : InfoEntryCompanion<Instant> {
    private const val ID = "last_update"

    context(Transaction)
    override fun get(): Instant? = InfoEntry.findById(ID)?.value?.toLong()?.let(Instant::ofEpochMilli)

    context(Transaction)
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

    context(Transaction)
    fun set(value: Instant = Instant.now()) = update(value)
}
