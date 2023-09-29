package com.arnyminerz.escalaralcoiaicomtat.backend.database.migration

import com.arnyminerz.escalaralcoiaicomtat.backend.database.entity.info.DatabaseVersion
import org.jetbrains.exposed.sql.Transaction

abstract class Migration(
    val from: Int?,
    val to: Int
) {
    context (Transaction)
    suspend fun performMigration() {
        migrate()

        DatabaseVersion.update(to)
    }

    protected abstract suspend fun Transaction.migrate()
}
