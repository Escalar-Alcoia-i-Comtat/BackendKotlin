package database.migration

import org.jetbrains.exposed.sql.Transaction

object Migrate1To2 : Migration(from = 1, to = 2) {
    override suspend fun Transaction.migrate() {
        exec("ALTER TABLE Sectors ADD COLUMN tracks TEXT DEFAULT NULL;")
    }
}
