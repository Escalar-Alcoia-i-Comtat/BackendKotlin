package database.table

import data.ExternalTrack
import data.PhoneSignalAvailability
import database.SqlConsts
import database.entity.Sector
import database.serialization.Json
import kotlinx.serialization.builtins.ListSerializer
import org.jetbrains.exposed.sql.json.json

object Sectors : BaseTable() {
    val displayName = varchar("display_name", SqlConsts.DISPLAY_NAME_LENGTH)

    val imagePath = varchar("image", SqlConsts.FILE_LENGTH)
    val gpxPath = varchar("gpx", SqlConsts.FILE_LENGTH).nullable()
    val tracks = json("tracks", Json, ListSerializer(ExternalTrack.serializer())).nullable().default(null)

    val latitude = double("latitude").nullable()
    val longitude = double("longitude").nullable()

    val kidsApt = bool("kids_apt")
    val sunTime = enumeration<Sector.SunTime>("sun_time")
    val walkingTime = uinteger("walking_time").nullable()
    val weight = varchar("weight", SqlConsts.WEIGHT_LENGTH).default("0000")

    val phoneSignalAvailability = json(
        "phone_signal_availability",
        Json,
        ListSerializer(PhoneSignalAvailability.serializer())
    ).nullable().default(null)

    val zone = reference("zone", Zones)
}
