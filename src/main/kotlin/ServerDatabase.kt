import database.entity.info.Version
import database.migration.Migration
import database.table.Areas
import database.table.BlockingTable
import database.table.InfoTable
import database.table.Paths
import database.table.Sectors
import database.table.Zones
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlLogger
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import system.EnvironmentVariables

/**
 * A utility class for interacting with the database.
 *
 * Must only be initialized once, so [ServerDatabase.instance] must be used to access the instance.
 */
class ServerDatabase private constructor() {
    companion object {
        /**
         * The URL of the target database. Defaults to an in-memory H2 database.
         * **Doesn't work in production.**
         */
        var url: String = "jdbc:h2:mem:regular;DB_CLOSE_DELAY=-1;"

        /**
         * The driver to use for performing connections with the database. Defaults to H2.
         * **Doesn't work in production.**
         */
        var driver: String = "org.h2.Driver"

        /** The username to use for connecting to the database. */
        var username: String = ""

        /** The password to use for connecting to the database. */
        var password: String = ""

        /** The logger to be used in the database. */
        var logger: SqlLogger? = null

        /**
         * Gives access to the database instance. Gets initialized lazily the first time it's fetched. Uses the
         * configuration set in [url], [driver], [username] and [password]. Once fetched, updates to these properties
         * are ignored.
         */
        val instance by lazy { ServerDatabase() }

        val version = Migration.all.maxOf { it.to }

        /**
         * Configures the database connection parameters from the environment variables.
         *
         * The method reads the following environment variables:
         * - `DATABASE_URL`: The URL of the database server.
         * - `DATABASE_DRIVER`: The driver class name for connecting to the database.
         * - `DATABASE_USERNAME`: The username for authenticating the database connection.
         * - `DATABASE_PASSWORD`: The password for authenticating the database connection.
         *
         * If any of the environment variables are not set or empty, the corresponding
         * connection parameter for the database is not changed.
         *
         * Usage:
         * ```
         * configureFromEnvironment()
         **/
        fun configureFromEnvironment() {
            EnvironmentVariables.Database.Url.value?.let { url = it }
            EnvironmentVariables.Database.Driver.value?.let { driver = it }
            EnvironmentVariables.Database.Username.value?.let { username = it }
            EnvironmentVariables.Database.Password.value?.let { password = it }
        }

        suspend operator fun <T> invoke(block: suspend Transaction.() -> T): T = instance.query(block)

        val tables = sequenceOf(Areas, Zones, Sectors, Paths, BlockingTable, InfoTable)
    }

    private val database by lazy {
        Database.connect(url, driver, username, password)
    }

    /**
     * Creates all the missing tables and columns for the database.
     * Should be run as soon as possible in the program's lifecycle.
     */
    suspend fun initialize() = invoke {
        val existingTables = SchemaUtils.listTables()
        for (table in tables) {
            val tableName = table.nameInDatabaseCase()
            if (!existingTables.contains(tableName)) {
                Logger.debug("Creating ${tableName}...")
                execInBatch(table.createStatement())
            } else {
                Logger.debug("- $tableName already exists")
            }
        }

        if (Version.isInitialized()) {
            var loops = 0
            while (Version.updateRequired()) {
                check(loops++ <= version) { "Version update loop detected" }
                val version = Version.get()
                val migration = Migration.all.find { it.from == version }
                    ?: error("No migration found for version $version")
                Logger.info("Migrating database from version $version to ${migration.to}")
                with(migration) { this@invoke() }
            }
        } else {
            Logger.info("Version not initialized, setting to $version")
            Version.set(version)
        }
    }

    suspend operator fun <T> invoke(block: suspend Transaction.() -> T): T = query(block)

    suspend fun <T> query(block: suspend Transaction.() -> T): T = newSuspendedTransaction(Dispatchers.IO, database) {
        logger?.let { addLogger(it) }

        block()
    }

    /**
     * Begins a transaction and runs the given block of code in it.
     *
     * @param block The block of code to run in the transaction.
     */
    fun <T> querySync(block: Transaction.() -> T) = transaction(database) {
        logger?.let { addLogger(it) }

        block()
    }
}
