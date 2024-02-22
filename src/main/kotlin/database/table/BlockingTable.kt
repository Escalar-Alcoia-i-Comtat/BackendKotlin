package database.table

import data.BlockingTypes
import java.time.Month
import org.jetbrains.exposed.sql.javatime.datetime

object BlockingTable: BaseTable() {
    val type = enumeration<BlockingTypes>("type")

    val fromDay = ushort("from_day").nullable()
    val fromMonth = enumeration<Month>("from_month").nullable()
    val toDay = ushort("to_day").nullable()
    val toMonth = enumeration<Month>("to_month").nullable()

    val endDate = datetime("end_date").nullable()

    val path = reference("path", Paths)
}
