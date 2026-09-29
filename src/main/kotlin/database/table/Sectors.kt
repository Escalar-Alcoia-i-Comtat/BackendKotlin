package database.table

import data.ExternalTrack
import data.PhoneSignalAvailability
import data.Topo
import database.SqlConsts
import database.entity.Sector
import database.serialization.Json
import kotlinx.serialization.builtins.ListSerializer
import org.jetbrains.exposed.v1.json.json

object Sectors : BaseTable() {
    val displayName = varchar("display_name", SqlConsts.DISPLAY_NAME_LENGTH)

    val imagePath = varchar("image", SqlConsts.FILE_LENGTH)
    val gpxPath = varchar("gpx", SqlConsts.FILE_LENGTH).nullable()

    /** Route drawing as data. When set, the sector's image is rendered from it on top of [topoImagePath]. */
    val topo = json("topo", Topo.json, Topo.serializer()).nullable().default(null)
    val topoImagePath = varchar("topo_image", SqlConsts.FILE_LENGTH).nullable().default(null)
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
