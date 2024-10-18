package database.entity.info

import java.time.Instant
import org.jetbrains.exposed.sql.Transaction

object LastUpdate : InfoEntryCompanion<Instant> {
    private const val ID = "last_update"

    override fun Transaction.get(): Instant? = InfoEntry.findById(ID)?.value?.toLong()?.let(Instant::ofEpochMilli)

    override fun Transaction.update(value: Instant) {
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

    fun Transaction.set(value: Instant = Instant.now()) = update(value)
}
