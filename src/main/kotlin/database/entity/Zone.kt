package database.entity

import data.DataPoint
import data.LatLng
import database.serialization.ZoneSerializer
import database.table.Zones
import java.io.File
import java.net.URL
import java.time.Instant
import kotlinx.serialization.Serializable
import org.jetbrains.annotations.VisibleForTesting
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import server.response.ResponseData
import storage.Storage

/**
 * Represents a Zone entity.
 * Extends the DataEntity class and implements the JsonSerializable interface.
 *
 * @param id The ID of the zone.
 */
@Serializable(with = ZoneSerializer::class)
class Zone(id: EntityID<Int>): DataEntity(id), ResponseData {
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

    var point: LatLng? by Zones.point

    var points: List<DataPoint> by Zones.points

    var area by Area referencedOn Zones.area

    private var _image: String by Zones.imagePath
    private var _kmz: String by Zones.kmzPath

    private var _webUrl: String by Zones.webUrl


    // Kept for migration, do not use
    @set:VisibleForTesting
    @Suppress("Deprecation")
    @Deprecated("Do not use. Only for migration")
    var latitude: Double? by Zones.latitude

    @set:VisibleForTesting
    @Suppress("Deprecation")
    @Deprecated("Do not use. Only for migration")
    var longitude: Double? by Zones.longitude

    @set:VisibleForTesting
    @Suppress("Deprecation")
    @Deprecated("Do not use. Only for migration")
    var pointsString: String by Zones.pointsString
    // End of migration


    var sectors: List<Sector>? = null
        private set

    /**
     * Updates the value of [sectors] with the given list of [sectors], filtering the ones that belong to this sector.
     * Calls [Sector.populatePaths] on each child.
     *
     * **Must be in a transaction to use**
     */
    fun populateSectors(sectors: Iterable<Sector>, paths: Iterable<Path>) {
        this.sectors = sectors.filter { it.zone.id == id }.onEach { it.populatePaths(paths) }
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
        result = 31 * result + points.hashCode()
        result = 31 * result + area.hashCode()
        return result
    }


}
