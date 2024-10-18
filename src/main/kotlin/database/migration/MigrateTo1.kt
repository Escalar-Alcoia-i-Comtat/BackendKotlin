package database.migration

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

    private fun Transaction.migrateZones() {
        Zone.all().forEach { zone ->
            // Move from latitude and longitude to point
            val lat = zone.latitude
            val lon = zone.longitude
            if (lat != null && lon != null) zone.point = LatLng(lat, lon)
            zone.latitude = null
            zone.longitude = null

            // Move from pointsString to points
            val pointsString = zone.pointsString
            if (pointsString == "\u0000") {
                zone.points = emptyList()
            } else if (!pointsString.isNullOrEmpty()) {
                // Points were saved sometimes in a wrong JSON format. Instead of arrays, they were objects in multiple lines.
                if (pointsString.startsWith("{")) {
                    pointsString.split('\n').joinToString(",") { it.trim() }.let {
                        zone.points = Json.decodeFromString("[$it]")
                    }
                } else {
                    zone.points = Json.decodeFromString(pointsString)
                }
            }
            zone.pointsString = null
        }
    }
}
