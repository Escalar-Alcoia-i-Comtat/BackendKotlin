package com.arnyminerz.escalaralcoiaicomtat.backend.database.table

import com.arnyminerz.escalaralcoiaicomtat.backend.database.SqlConsts.FILE_LENGTH

object Areas : DataTable() {
    val imagePath = varchar("image", FILE_LENGTH)
}
