package database.table

import data.DataPoint
import data.LatLng
import database.SqlConsts
import database.serialization.Json
import org.jetbrains.exposed.sql.json.json

object Zones : DataTable() {
    val imagePath = varchar("image", SqlConsts.FILE_LENGTH)
    val kmzPath = varchar("kmz", SqlConsts.FILE_LENGTH)

    val point = json<LatLng>("point", Json).nullable()

    val points = json<List<DataPoint>>("points_list", Json).default(emptyList())

    @Deprecated("Use point instead. Kept for migration purposes.")
    val latitude = double("latitude").nullable()
    @Deprecated("Use point instead. Kept for migration purposes.")
    val longitude = double("longitude").nullable()
    @Deprecated("Use point instead. Kept for migration purposes.")
    val pointsString = varchar("points", SqlConsts.POINTS_LENGTH).nullable()

    val area = reference("area", Areas)
}
