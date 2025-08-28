package database.migration

import org.jetbrains.exposed.sql.Transaction

object Migrate2To3 : Migration(from = 2, to = 3) {
    override suspend fun Transaction.migrate() {
        // Add the phone_signal_availability column to the Sectors table
        exec("ALTER TABLE Sectors ADD COLUMN phone_signal_availability TEXT DEFAULT NULL;")
    }
}
