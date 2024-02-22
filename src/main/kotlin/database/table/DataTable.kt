package database.table

import database.SqlConsts

abstract class DataTable : BaseTable() {
    val displayName = varchar("display_name", SqlConsts.DISPLAY_NAME_LENGTH)
    val webUrl = varchar("web_url", SqlConsts.URL_LENGTH)
}
