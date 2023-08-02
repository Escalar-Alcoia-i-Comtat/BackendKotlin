package com.arnyminerz.escalaralcoiaicomtat.backend.database.entity

import com.arnyminerz.escalaralcoiaicomtat.backend.ServerDatabase
import com.arnyminerz.escalaralcoiaicomtat.backend.data.LatLng
import com.arnyminerz.escalaralcoiaicomtat.backend.database.table.Sectors
import com.arnyminerz.escalaralcoiaicomtat.backend.storage.Storage
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.jsonOf
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.serialization.JsonSerializable
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.toJson
import java.io.File
import java.time.Instant
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.json.JSONObject

/**
 * Represents a Sector entity.
 * Extends the DataEntity class and implements the JsonSerializable interface.
 *
 * @param id The ID of the sector.
 */
class Sector(id: EntityID<Int>): BaseEntity(id), JsonSerializable {
    companion object: IntEntityClass<Sector>(Sectors)

    override var timestamp: Instant by Sectors.timestamp

    var displayName: String by Sectors.displayName

    var kidsApt: Boolean by Sectors.kidsApt
    var sunTime: SunTime by Sectors.sunTime
    var walkingTime: UInt? by Sectors.walkingTime
    var weight: String by Sectors.weight

    var image: File
        get() = File(Storage.ImagesDir, _image)
        set(value) { _image = value.toRelativeString(Storage.ImagesDir) }

    var point: LatLng?
        get() = _latitude?.let { lat -> _longitude?.let { lon -> LatLng(lat, lon) } }
        set(value) { _latitude = value?.latitude; _longitude = value?.longitude }

    var zone by Zone referencedOn Sectors.zone

    private var _image: String by Sectors.imagePath

    private var _latitude: Double? by Sectors.latitude
    private var _longitude: Double? by Sectors.longitude

    /**
     * Converts the object to a JSON representation.
     *
     * **Must be in a database transaction**.
     *
     * See [ServerDatabase.query].
     *
     * Structure:
     * - `id`: [id] ([Int])
     * - `timestamp`: [timestamp] ([Long])
     * - `display_name`: [displayName] ([String])
     * - `kids_apt`: [kidsApt] ([Boolean])
     * - `sun_time`: [sunTime] ([SunTime])
     * - `walking_time`: [walkingTime] ([Int]|`null`)
     * - `image`: [image] ([String])
     * - `point`: [point] ([String])
     * - `zone_id`: [zone] ([Int])
     *
     * @return The JSON object representing the object.
     */
    override fun toJson(): JSONObject = jsonOf(
        "id" to id.value,
        "timestamp" to timestamp.toEpochMilli(),
        "display_name" to displayName,
        "kids_apt" to kidsApt,
        "sun_time" to sunTime,
        "walking_time" to walkingTime,
        "image" to _image,
        "point" to point,
        "zone_id" to zone.id.value
    )

    /**
     * Uses [toJson] to convert the data into a [JSONObject], but adds a new key called `paths` with the data of the
     * paths.
     *
     * **Must be in a transaction to use**
     */
    fun toJsonWithPaths(): JSONObject = toJson().apply {
        val paths = Path.all().filter { it.sector.id == id }
        put("paths", paths.toJson())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Sector

        return id == other.id
    }

    override fun hashCode(): Int {
        var result = timestamp.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + displayName.hashCode()
        result = 31 * result + kidsApt.hashCode()
        result = 31 * result + sunTime.hashCode()
        result = 31 * result + (walkingTime?.hashCode() ?: 0)
        result = 31 * result + image.hashCode()
        result = 31 * result + (point?.hashCode() ?: 0)
        result = 31 * result + zone.hashCode()
        return result
    }


    /**
     * Represents different times of the day.
     */
    enum class SunTime {
        None, Morning, Afternoon, Day
    }
}
