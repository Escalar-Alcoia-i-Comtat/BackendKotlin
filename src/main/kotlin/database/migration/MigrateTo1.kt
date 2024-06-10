package database.migration

import data.DataPoint
import data.LatLng
import database.entity.Zone
import database.serialization.Json
import org.jetbrains.exposed.sql.Transaction

@Suppress("DEPRECATION")
object MigrateTo1 : Migration(null, 1) {
    override suspend fun Transaction.migrate() {
        // Migrate Zone members
        migrateZones()
    }

    private fun migrateZones() {
        Zone.all().forEach { zone ->
            val lat = zone.latitude
            val lon = zone.longitude
            if (lat != null && lon != null) zone.point = LatLng(lat, lon)

            val points = zone.pointsString
            zone.points = Json.decodeFromString<List<DataPoint>>(points)
        }
    }
}
