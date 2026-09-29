package database.migration

import database.entity.info.Version
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction

/**
 * Defines how a migration between different versions of the database should be handled.
 * @param from The version to migrate from. There should really be only one migration with this value as null. It will
 * be used to migrate from the initial version of the database. The other ones will be handled sequentially.
 * @param to The version to migrate to.
 */
abstract class Migration(
    val from: Int?,
    val to: Int
) {
    companion object {
        val all: List<Migration> = listOf(MigrateTo1, Migrate1To2, Migrate2To3)
    }

    protected abstract suspend fun JdbcTransaction.migrate()

    suspend operator fun JdbcTransaction.invoke() {
        migrate()

        // Update the version of the database
        Version.set(to)
    }
}
