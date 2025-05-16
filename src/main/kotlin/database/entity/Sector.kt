package database.entity

import data.LatLng
import database.serialization.SectorSerializer
import database.table.Paths
import database.table.Sectors
import java.io.File
import java.time.Instant
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import server.response.ResponseData
import storage.Storage

/**
 * Represents a Sector entity.
 * Extends the DataEntity class and implements the JsonSerializable interface.
 *
 * @param id The ID of the sector.
 */
@Serializable(with = SectorSerializer::class)
class Sector(id: EntityID<Int>): BaseEntity(id), ResponseData {
    companion object: IntEntityClass<Sector>(Sectors)

    override var timestamp: Instant by Sectors.timestamp

    var displayName: String by Sectors.displayName

    var kidsApt: Boolean by Sectors.kidsApt
    var sunTime: SunTime by Sectors.sunTime
    var walkingTime: UInt? by Sectors.walkingTime
    var weight: String by Sectors.weight

    private fun findFileByUUID(uuid: String, dir: File): File {
        return dir.listFiles { _, name -> name.startsWith(uuid) }
            ?.firstOrNull()
            ?: throw IllegalArgumentException("File ($uuid) not found in $dir")
    }

    var image: File
        get() = findFileByUUID(_image, Storage.ImagesDir)
        set(value) { _image = value.toRelativeString(Storage.ImagesDir) }

    var gpx: File?
        get() = _gpx?.let { findFileByUUID(it, Storage.TracksDir) }
        set(value) { _gpx = value?.toRelativeString(Storage.TracksDir) }

    var tracks by Sectors.tracks

    var point: LatLng?
        get() = _latitude?.let { lat -> _longitude?.let { lon -> LatLng(lat, lon) } }
        set(value) { _latitude = value?.latitude; _longitude = value?.longitude }

    var zone by Zone referencedOn Sectors.zone

    private var _image: String by Sectors.imagePath
    private var _gpx: String? by Sectors.gpxPath

    private var _latitude: Double? by Sectors.latitude
    private var _longitude: Double? by Sectors.longitude

    var paths: List<Path>? = null
        private set

    /**
     * Updates the value of [paths] with the given list of [paths], filtering the ones that belong to this sector.
     *
     * **Must be in a transaction to use**
     */
    fun populatePaths(paths: Iterable<Path>) {
        this.paths = paths.filter { it.sector.id == id }
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
        result = 31 * result + (gpx?.hashCode() ?: 0)
        result = 31 * result + (tracks?.hashCode() ?: 0)
        result = 31 * result + (point?.hashCode() ?: 0)
        result = 31 * result + (weight.hashCode())
        result = 31 * result + zone.id.value.hashCode()
        return result
    }

    /** **Must be in a transaction.** */
    fun deleteRecursively() {
        for (path in Path.find { Paths.sector eq id }) {
            path.delete()
        }
        delete()
    }


    /**
     * Represents different times of the day.
     */
    @Serializable
    enum class SunTime {
        None, Morning, Afternoon, Day
    }
}
