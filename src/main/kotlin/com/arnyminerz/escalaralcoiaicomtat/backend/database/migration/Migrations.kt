package com.arnyminerz.escalaralcoiaicomtat.backend.database.migration

import com.arnyminerz.escalaralcoiaicomtat.backend.Logger
import com.arnyminerz.escalaralcoiaicomtat.backend.ServerDatabase
import com.arnyminerz.escalaralcoiaicomtat.backend.database.entity.info.DatabaseVersion

object Migrations {
    const val DATABASE_VERSION = 1

    private val values = arrayOf<Migration>(To1)

    suspend fun runMigrations() {
        ServerDatabase.instance.query {
            var currentVersion = DatabaseVersion.get()
            while (currentVersion != DATABASE_VERSION) {
                val migration = values.find { it.from == currentVersion }
                require(migration != null) {
                    "Could not find migration from version $currentVersion. Database version: $DATABASE_VERSION"
                }
                migration.performMigration()

                val newVersion = DatabaseVersion.get()
                require(newVersion != currentVersion) { "Migration unsuccessful" }
                currentVersion = newVersion
            }
            Logger.info("Database is up to date.")
        }
    }
}
