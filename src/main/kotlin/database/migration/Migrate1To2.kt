package database.migration

import java.sql.Timestamp
import org.jetbrains.exposed.sql.Transaction

object Migrate1To2 : Migration(from = 1, to = 2) {
    override suspend fun Transaction.migrate() {
        // Add the tracks column to the Sectors table
        exec("ALTER TABLE Sectors ADD COLUMN tracks TEXT DEFAULT NULL;")

        // Change the default value of timestamp in all tables to be CURRENT_TIMESTAMP
        val tables = ServerDatabase.tables.map { it.nameInDatabaseCase() }
        for (tableName in tables) {
            Logger.warn("Updating default value of timestamp for $tableName...")

            exec("ALTER TABLE $tableName RENAME COLUMN timestamp TO timestamp_old;")
            exec("ALTER TABLE $tableName ADD COLUMN timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP;")

            val updates = mutableMapOf<Int, Timestamp>()
            exec("SELECT id, timestamp_old FROM $tableName WHERE timestamp_old IS NOT NULL;") { rs ->
                while (rs.next()) {
                    val id = rs.getInt("id")
                    val timestamp = rs.getTimestamp("timestamp_old")
                    updates += id to timestamp
                }
            }
            Logger.warn("Copying timestamp of ${updates.size} rows in $tableName...")
            execInBatch(
                updates.map { (id, timestamp) -> "UPDATE $tableName SET timestamp = '$timestamp' WHERE id = $id;" }
            )

            exec("ALTER TABLE $tableName DROP COLUMN timestamp_old;")
        }
    }
}
