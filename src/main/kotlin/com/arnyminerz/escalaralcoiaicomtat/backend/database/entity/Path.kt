package com.arnyminerz.escalaralcoiaicomtat.backend.database.entity

import com.arnyminerz.escalaralcoiaicomtat.backend.data.GradeValue
import com.arnyminerz.escalaralcoiaicomtat.backend.data.PitchInfo
import com.arnyminerz.escalaralcoiaicomtat.backend.database.table.Paths
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.json
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.jsonOf
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.serialization.JsonSerializable
import java.time.Instant
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

    var pitches: List<PitchInfo>?
        get() = _pitches
            ?.split("\n")
            ?.map { PitchInfo.fromJson(it.json) }
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

    var sector by Sector referencedOn Paths.sector


    private var _grade: String? by Paths.grade
    private var _pitches: String? by Paths.pitches

    override fun toJson(): JSONObject = jsonOf()
}
