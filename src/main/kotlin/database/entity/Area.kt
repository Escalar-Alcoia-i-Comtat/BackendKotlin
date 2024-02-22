package database.entity

import database.table.Areas
import java.io.File
import java.net.URL
import java.time.Instant
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.json.JSONObject
import storage.Storage
import utils.jsonOf
import utils.mapJson
import utils.serialization.JsonSerializable
import utils.toJson

/**
 * Represents the data structure of an Area, which contains Zones.
 */
class Area(id: EntityID<Int>) : DataEntity(id), JsonSerializable {
    companion object : IntEntityClass<Area>(Areas)

    override var timestamp: Instant by Areas.timestamp
    override var displayName: String by Areas.displayName

    var image: File
        get() = File(Storage.ImagesDir, _image)
        set(value) {
            _image = value.toRelativeString(Storage.ImagesDir)
        }

    override var webUrl: URL
        get() = URL(_webUrl)
        set(value) {
            _webUrl = value.toString()
        }

    private var _image: String by Areas.imagePath

    private var _webUrl: String by Areas.webUrl


    override fun toJson(): JSONObject = jsonOf(
        "id" to id.value,
        "timestamp" to timestamp.toEpochMilli(),
        "display_name" to displayName,
        "image" to _image.substringBeforeLast('.'),
        "web_url" to webUrl
    )

    /**
     * Uses [toJson] to convert the data into a [JSONObject], but adds a new key called `zones` with the data of the
     * zones.
     * This method requires a list of [zones], [sectors] and [paths] which will be used for knowing the whole dataset.
     *
     * **Must be in a transaction to use**
     */
    fun toJsonWithZones(zones: Iterable<Zone>, sectors: Iterable<Sector>, paths: Iterable<Path>): JSONObject =
        toJson().apply {
            put("zones", zones.filter { it.area.id == id }.mapJson { it.toJsonWithSectors(sectors, paths) })
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Area

        return id == other.id
    }

    override fun hashCode(): Int {
        var result = timestamp.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + displayName.hashCode()
        result = 31 * result + image.hashCode()
        result = 31 * result + webUrl.toString().hashCode()
        return result
    }
}
