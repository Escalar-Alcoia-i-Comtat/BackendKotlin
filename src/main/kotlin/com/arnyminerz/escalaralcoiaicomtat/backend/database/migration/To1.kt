package com.arnyminerz.escalaralcoiaicomtat.backend.database.migration

import com.arnyminerz.escalaralcoiaicomtat.backend.database.SqlConsts
import com.arnyminerz.escalaralcoiaicomtat.backend.database.table.Paths
import org.jetbrains.exposed.sql.Transaction
import org.postgresql.util.PSQLException

object To1 : Migration(null, 1) {
    override suspend fun Transaction.migrate() {
        try {
            exec("ALTER TABLE Paths ADD images varchar(${SqlConsts.FILE_LENGTH * Paths.MAX_IMAGES});")
        } catch (_: PSQLException) {
        }
    }
}
