package com.arnyminerz.escalaralcoiaicomtat.backend.database.entity

import com.arnyminerz.escalaralcoiaicomtat.backend.data.Builder
import com.arnyminerz.escalaralcoiaicomtat.backend.data.Ending
import com.arnyminerz.escalaralcoiaicomtat.backend.data.GradeValue
import com.arnyminerz.escalaralcoiaicomtat.backend.data.PitchInfo
import com.arnyminerz.escalaralcoiaicomtat.backend.database.table.Paths
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.json
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.jsonArray
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.jsonOf
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.serialization.JsonSerializable
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.serialize
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.toJson
import java.time.Instant
import org.jetbrains.annotations.VisibleForTesting
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.json.JSONObject

class Path(id: EntityID<Int>): BaseEntity(id), JsonSerializable {
    companion object: IntEntityClass<Path>(Paths)

    override var timestamp: Instant by Paths.timestamp
    var displayName: String by Paths.displayName
    var sketchId: UInt by Paths.sketchId

    var height: UInt? by Paths.height
    var grade: GradeValue?
        get() = _grade?.let { GradeValue.fromString(it) }
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
            return _pitches
                ?.split("\n")
                ?.map { PitchInfo.fromJson(it.json) }
        }
        set(value) {
            _pitches = value?.joinToString("\n") { it.toJson().toString() }
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
            return _builder?.json?.let { Builder.fromJson(it) }
        }
        set(value) { _builder = value?.toJson().toString() }
    var reBuilder: List<Builder>?
        get() = _reBuilder?.jsonArray?.serialize(Builder)
        set(value) { _reBuilder = value?.toJson()?.toString() }

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

    override fun toJson(): JSONObject = jsonOf(
        "id" to id.value,
        "timestamp" to timestamp.toEpochMilli(),

        "display_name" to displayName,
        "sketch_id" to sketchId,

        "height" to height,
        "grade" to grade,
        "ending" to ending,

        "pitches" to pitches,

        "string_count" to stringCount,

        "parabolt_count" to paraboltCount,
        "buril_count" to burilCount,
        "piton_count" to pitonCount,
        "spit_count" to spitCount,
        "tensor_count" to tensorCount,

        "cracker_required" to crackerRequired,
        "friend_required" to friendRequired,
        "lanyard_required" to lanyardRequired,
        "nail_required" to nailRequired,
        "piton_required" to pitonRequired,
        "stapes_required" to stapesRequired,

        "show_description" to showDescription,
        "description" to description,

        "builder" to builder,
        "re_builder" to reBuilder,

        "sector_id" to sector.id.value
    )

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
        result = 31 * result + sector.hashCode()
        return result
    }


}
