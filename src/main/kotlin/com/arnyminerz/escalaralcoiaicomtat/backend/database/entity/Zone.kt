package com.arnyminerz.escalaralcoiaicomtat.backend.database.entity

import com.arnyminerz.escalaralcoiaicomtat.backend.data.DataPoint
import com.arnyminerz.escalaralcoiaicomtat.backend.data.LatLng
import com.arnyminerz.escalaralcoiaicomtat.backend.database.table.Zones
import com.arnyminerz.escalaralcoiaicomtat.backend.storage.Storage
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.json
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.jsonOf
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.serialization.JsonSerializable
import java.io.File
import java.net.URL
import java.time.Instant
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.json.JSONObject

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
        get() = _points.split("\n")
        set(value) { _points = value.joinToString("\n") }

    var points: MutableSet<DataPoint> = object : MutableSet<DataPoint> {
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
            pointsSet.map { DataPoint.fromJson(it.json) }.toMutableSet().iterator()

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

    override fun toJson(): JSONObject = jsonOf(
        "id" to id.value,
        "timestamp" to timestamp.toEpochMilli(),
        "display_name" to displayName,
        "web_url" to webUrl,
        "point" to point,
        "points" to points,
        "area_id" to area.id.value
    )
}