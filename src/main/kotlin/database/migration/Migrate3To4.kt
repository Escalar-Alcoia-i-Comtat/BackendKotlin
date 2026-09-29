package database.migration

import database.SqlConsts
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction

object Migrate3To4 : Migration(from = 3, to = 4) {
    override suspend fun JdbcTransaction.migrate() {
        // Add the topo columns to the Sectors table
        exec("ALTER TABLE Sectors ADD COLUMN topo TEXT DEFAULT NULL;")
        exec("ALTER TABLE Sectors ADD COLUMN topo_image VARCHAR(${SqlConsts.FILE_LENGTH}) DEFAULT NULL;")
    }
}
