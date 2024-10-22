package database.entity

import data.Builder
import data.Ending
import data.Grade
import data.PitchInfo
import database.serialization.Json
import database.serialization.PathSerializer
import database.table.Paths
import java.io.File
import java.time.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.encodeToString
import org.jetbrains.annotations.VisibleForTesting
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import server.response.ResponseData
import storage.Storage

@Serializable(with = PathSerializer::class)
class Path(id: EntityID<Int>): BaseEntity(id), ResponseData {
    companion object: IntEntityClass<Path>(Paths)

    override var timestamp: Instant by Paths.timestamp
    var displayName: String by Paths.displayName
    var sketchId: UInt by Paths.sketchId

    var height: UInt? by Paths.height
    var grade: Grade?
        get() = _grade?.let { Grade.fromString(it) }
        set(value) { _grade = value?.name }
    var ending: Ending?
        get() = _ending?.let { Ending.valueOf(it) }
        set(value) { _ending = value?.name }

    var pitches: List<PitchInfo>?
        get() {
            // This is invalid, clear
            if (_pitches == "{\"pitch\":\"0\"}") {
                _pitches = null
                return null
            }
            // If is blank, should be null, update to null
            if (_pitches.isNullOrBlank()) {
                _pitches = null
                return null
            }
            return _pitches?.let { Json.decodeFromString<List<PitchInfo>>(it) }
        }
        set(value) {
            _pitches = value?.let { Json.encodeToString(ListSerializer(PitchInfo.serializer()), it) }
        }

    var stringCount: UInt? by Paths.stringCount

    var paraboltCount: UInt? by Paths.paraboltCount
    var burilCount: UInt? by Paths.burilCount
    var pitonCount: UInt? by Paths.pitonCount
    var spitCount: UInt? by Paths.spitCount
    var tensorCount: UInt? by Paths.tensorCount

    var crackerRequired: Boolean by Paths.crackerRequired
    var friendRequired: Boolean by Paths.friendRequired
    var lanyardRequired: Boolean by Paths.lanyardRequired
    var nailRequired: Boolean by Paths.nailRequired
    var pitonRequired: Boolean by Paths.pitonRequired
    var stapesRequired: Boolean by Paths.stapesRequired

    var showDescription: Boolean by Paths.showDescription
    var description: String? by Paths.description

    var builder: Builder?
        get() {
            if (_builder.equals("null", true)) {
                _builder = null
                return null
            }
            return _builder?.let { Json.decodeFromString(it) }
        }
        set(value) { _builder = value?.let { Json.encodeToString(it) } }
    var reBuilder: List<Builder>?
        get() = _reBuilder?.let { Json.decodeFromString(it) }
        set(value) { _reBuilder = value?.let { Json.encodeToString(ListSerializer(Builder.serializer()), it) } }

    var images: List<File>?
        get() = _images?.split('\n')?.map { File(Storage.ImagesDir, it) }
        set(value) { _images = value?.joinToString("\n") { it.toRelativeString(Storage.ImagesDir) } }

    var sector by Sector referencedOn Paths.sector


    private var _grade: String? by Paths.grade
    private var _ending: String? by Paths.ending

    @Suppress("VariableNaming", "PropertyName")
    @get:VisibleForTesting
    @set:VisibleForTesting
    var _pitches: String? by Paths.pitches

    @Suppress("VariableNaming", "PropertyName")
    @get:VisibleForTesting
    @set:VisibleForTesting
    var _builder: String? by Paths.builder
    private var _reBuilder: String? by Paths.reBuilder

    private var _images: String? by Paths.images

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Path

        return id == other.id
    }

    override fun hashCode(): Int {
        var result = timestamp.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + displayName.hashCode()
        result = 31 * result + sketchId.hashCode()
        result = 31 * result + (height?.hashCode() ?: 0)
        result = 31 * result + (grade?.hashCode() ?: 0)
        result = 31 * result + (ending?.hashCode() ?: 0)
        result = 31 * result + (pitches?.hashCode() ?: 0)
        result = 31 * result + (stringCount?.hashCode() ?: 0)
        result = 31 * result + (paraboltCount?.hashCode() ?: 0)
        result = 31 * result + (burilCount?.hashCode() ?: 0)
        result = 31 * result + (pitonCount?.hashCode() ?: 0)
        result = 31 * result + (spitCount?.hashCode() ?: 0)
        result = 31 * result + (tensorCount?.hashCode() ?: 0)
        result = 31 * result + crackerRequired.hashCode()
        result = 31 * result + friendRequired.hashCode()
        result = 31 * result + lanyardRequired.hashCode()
        result = 31 * result + nailRequired.hashCode()
        result = 31 * result + pitonRequired.hashCode()
        result = 31 * result + stapesRequired.hashCode()
        result = 31 * result + showDescription.hashCode()
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + (builder?.hashCode() ?: 0)
        result = 31 * result + (reBuilder?.hashCode() ?: 0)
        result = 31 * result + (_images?.hashCode() ?: 0)
        result = 31 * result + sector.id.value.hashCode()
        return result
    }


}
