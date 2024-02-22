package database.table

import database.SqlConsts.FILE_LENGTH

object Areas : DataTable() {
    val imagePath = varchar("image", FILE_LENGTH)
}
