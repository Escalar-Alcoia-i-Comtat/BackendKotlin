package database.entity

import ServerDatabase
import data.DataPoint
import data.LatLng
import database.table.Zones
import java.io.File
import java.net.URL
import java.time.Instant
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.json.JSONObject
import storage.Storage
import utils.json
import utils.jsonOf
import utils.mapJson
import utils.serialization.JsonSerializable
import utils.toJson

/**
 * Represents a Zone entity.
 * Extends the DataEntity class and implements the JsonSerializable interface.
 *
 * @param id The ID of the zone.
 */
class Zone(id: EntityID<Int>): DataEntity(id), JsonSerializable {
    companion object: IntEntityClass<Zone>(Zones)

    override var timestamp: Instant by Zones.timestamp
    override var displayName: String by Zones.displayName

    var image: File
        get() = File(Storage.ImagesDir, _image)
        set(value) { _image = value.toRelativeString(Storage.ImagesDir) }

    var kmz: File
        get() = File(Storage.TracksDir, _kmz)
        set(value) { _kmz = value.toRelativeString(Storage.TracksDir) }

    override var webUrl: URL
        get() = URL(_webUrl)
        set(value) { _webUrl = value.toString() }

    var point: LatLng?
        get() = _latitude?.let { lat -> _longitude?.let { lon -> LatLng(lat, lon) } }
        set(value) { _latitude = value?.latitude; _longitude = value?.longitude }

    var pointsSet: List<String>
        get() = _points.split("\n").filter { it.isNotBlank() }
        set(value) { _points = value.joinToString("\n") }

    val points: MutableSet<DataPoint> = object : MutableSet<DataPoint> {
        private val mutablePoints: MutableList<String>
            get() = pointsSet.toMutableList()

        override fun add(element: DataPoint): Boolean =
            mutablePoints.add(element.toJson().toString()).also {
                pointsSet = mutablePoints
            }

        override fun addAll(elements: Collection<DataPoint>): Boolean =
            mutablePoints.addAll(elements.map { it.toJson().toString() }).also {
                pointsSet = mutablePoints
            }

        override val size: Int
            get() = pointsSet.size

        override fun clear() {
            mutablePoints.clear().also {
                pointsSet = mutablePoints
            }
        }

        override fun isEmpty(): Boolean = pointsSet.isEmpty()

        override fun containsAll(elements: Collection<DataPoint>): Boolean =
            elements.all { pointsSet.contains(it.toJson().toString()) }

        override fun contains(element: DataPoint): Boolean =
            pointsSet.contains(element.toJson().toString())

        override fun iterator(): MutableIterator<DataPoint> =
            pointsSet
                .filter { it.isNotBlank() }
                .map { DataPoint.fromJson(it.json) }
                .toMutableSet()
                .iterator()

        override fun retainAll(elements: Collection<DataPoint>): Boolean =
            mutablePoints.retainAll(elements.map { it.toJson().toString() }).also {
                pointsSet = mutablePoints
            }

        override fun removeAll(elements: Collection<DataPoint>): Boolean =
            mutablePoints.removeAll(elements.map { it.toJson().toString() }).also {
                pointsSet = mutablePoints
            }

        override fun remove(element: DataPoint): Boolean =
            mutablePoints.remove(element.toJson().toString()).also {
                pointsSet = mutablePoints
            }
    }

    var area by Area referencedOn Zones.area

    private var _image: String by Zones.imagePath
    private var _kmz: String by Zones.kmzPath

    private var _webUrl: String by Zones.webUrl

    private var _latitude: Double? by Zones.latitude
    private var _longitude: Double? by Zones.longitude

    private var _points: String by Zones.points

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
     * - `image`: [image] ([String])
     * - `kmz`: [kmz] ([String])
     * - `web_url`: [webUrl] ([String])
     * - `point`: [point] ([String])
     * - `points`: [points] ([String])
     * - `area_id`: [area] ([Int])
     *
     * @return The JSON object representing the object.
     */
    override fun toJson(): JSONObject = jsonOf(
        "id" to id.value,
        "timestamp" to timestamp.toEpochMilli(),
        "display_name" to displayName,
        "image" to _image.substringBeforeLast('.'),
        "kmz" to _kmz.substringBeforeLast('.'),
        "web_url" to webUrl,
        "point" to point,
        "points" to points,
        "area_id" to area.id.value
    )

    /**
     * Uses [toJson] to convert the data into a [JSONObject], but adds a new key called `zones` with the data of the
     * zones.
     * This method requires a list of [sectors] and [paths] which will be used for knowing the whole dataset.
     *
     * **Must be in a transaction to use**
     */
    fun toJsonWithSectors(sectors: Iterable<Sector>, paths: Iterable<Path>): JSONObject = toJson().apply {
        put("sectors", sectors.filter { it.zone.id == id }.mapJson { it.toJsonWithPaths(paths) })
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Zone

        return id == other.id
    }

    override fun hashCode(): Int {
        var result = timestamp.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + displayName.hashCode()
        result = 31 * result + image.hashCode()
        result = 31 * result + kmz.hashCode()
        result = 31 * result + webUrl.toString().hashCode()
        result = 31 * result + (point?.hashCode() ?: 0)
        result = 31 * result + pointsSet.hashCode()
        result = 31 * result + points.hashCode()
        result = 31 * result + area.hashCode()
        return result
    }


}
