package com.arnyminerz.escalaralcoiaicomtat.backend.database.migration

import com.arnyminerz.escalaralcoiaicomtat.backend.Logger
import com.arnyminerz.escalaralcoiaicomtat.backend.database.SqlConsts
import com.arnyminerz.escalaralcoiaicomtat.backend.database.table.Paths
import org.jetbrains.exposed.sql.Transaction
import org.postgresql.util.PSQLException

object To1 : Migration(null, 1) {
    override suspend fun Transaction.migrate() {
        try {
            exec("ALTER TABLE Paths ADD images varchar(${SqlConsts.FILE_LENGTH * Paths.MAX_IMAGES});")
        } catch (e: PSQLException) {
            if (e.message?.contains("ERROR: column \"images\" of relation \"paths\" already exists") == true) {
                Logger.debug("Initial migration already has been run.")
            } else {
                throw e
            }
        }
    }
}
