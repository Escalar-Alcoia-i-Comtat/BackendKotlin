package database.entity

import database.serialization.AreaSerializer
import database.table.Areas
import database.table.Zones
import java.io.File
import java.net.URI
import java.net.URL
import java.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import server.response.ResponseData
import storage.Storage

/**
 * Represents the data structure of an Area, which contains Zones.
 */
@Serializable(with = AreaSerializer::class)
class Area(id: EntityID<Int>) : DataEntity(id), ResponseData {
    companion object : IntEntityClass<Area>(Areas)

    override var timestamp: Instant by Areas.timestamp
    @SerialName("display_name")
    override var displayName: String by Areas.displayName

    @Transient
    var image: File
        get() = File(Storage.ImagesDir, _image)
        set(value) {
            _image = value.toRelativeString(Storage.ImagesDir)
        }

    @Transient
    override var webUrl: URL
        get() = URI.create(_webUrl).toURL()
        set(value) {
            _webUrl = value.toString()
        }

    @SerialName("image")
    private var _image: String by Areas.imagePath

    @SerialName("web_url")
    private var _webUrl: String by Areas.webUrl

    var zones: List<Zone>? = null
        private set

    /**
     * Updates the value of [zones] with the given list of [zones], filtering the ones that belong to this sector.
     * Calls [Zone.populateSectors] on each child.
     *
     * **Must be in a transaction to use**
     */
    fun populateZones(zones: Iterable<Zone>, sectors: Iterable<Sector>, paths: Iterable<Path>) {
        this.zones = zones.filter { it.area.id == id }.onEach { it.populateSectors(sectors, paths) }
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

    /** **Must be in a transaction.** */
    fun deleteRecursively() {
        for (zone in Zone.find { Zones.area eq id }) {
            zone.deleteRecursively()
        }
        delete()
    }

}
