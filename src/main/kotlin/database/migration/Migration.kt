package database.migration

import org.jetbrains.exposed.sql.Transaction

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
        val all = listOf(MigrateTo1)
    }

    abstract suspend fun Transaction.migrate()
}
