package database.migration

import org.jetbrains.exposed.v1.jdbc.JdbcTransaction

object Migrate2To3 : Migration(from = 2, to = 3) {
    override suspend fun JdbcTransaction.migrate() {
        // Add the phone_signal_availability column to the Sectors table
        exec("ALTER TABLE Sectors ADD COLUMN phone_signal_availability TEXT DEFAULT NULL;")
    }
}
