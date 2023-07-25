package com.arnyminerz.escalaralcoiaicomtat.backend

import com.arnyminerz.escalaralcoiaicomtat.backend.ServerDatabase.Companion.tables
import com.arnyminerz.escalaralcoiaicomtat.backend.database.table.Areas
import com.arnyminerz.escalaralcoiaicomtat.backend.database.table.Sectors
import com.arnyminerz.escalaralcoiaicomtat.backend.database.table.Zones
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * A utility class for interacting with the database. Once initialized, automatically creates all the tables defined in
 * [tables]. Must only be initialized once, so [ServerDatabase.instance] must be used to access the instance.
 */
class ServerDatabase private constructor() {
    companion object {
        /** The URL of the target database. Defaults to an in-memory SQLite database. **Doesn't work in production.** */
        var url: String = "jdbc:sqlite:file:test?mode=memory&cache=shared"

        /**
         * The driver to use for performing connections with the database. Defaults to SQLite. **Doesn't work in
         * production.**
         */
        var driver: String = "org.sqlite.JDBC"

        /** The username to use for connecting to the database. */
        var user: String = ""

        /** The password to use for connecting to the database. */
        var password: String = ""

        /**
         * Gives access to the database instance. Gets initialized lazily the first time it's fetched. Uses the
         * configuration set in [url], [driver], [user] and [password]. Once fetched, updates to these properties are
         * ignored.
         */
        val instance by lazy { ServerDatabase() }

        /** All the tables to be created in the database. */
        private val tables: Array<Table> = arrayOf(Areas, Zones, Sectors)
    }

    private val database by lazy {
        Database.connect(url, driver, user, password)
    }

    init {
        transaction(database) {
            SchemaUtils.create(*tables)
        }
    }

    suspend fun <T> query(block: suspend () -> T): T = newSuspendedTransaction(Dispatchers.IO, database) {
        SchemaUtils.create(*tables)

        block()
    }
}
