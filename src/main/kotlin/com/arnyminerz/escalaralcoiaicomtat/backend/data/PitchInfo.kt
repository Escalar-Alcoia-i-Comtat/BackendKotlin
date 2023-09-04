package com.arnyminerz.escalaralcoiaicomtat.backend.data

import com.arnyminerz.escalaralcoiaicomtat.backend.utils.getEnumOrNull
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.getStringOrNull
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.getUInt
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.getUIntOrNull
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.jsonOf
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.serialization.JsonSerializable
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.serialization.JsonSerializer
import org.json.JSONObject

data class PitchInfo(
    val pitch: UInt,
    val gradeValue: GradeValue? = null,
    val height: UInt? = null,
    val ending: Ending? = null,
    val info: EndingInfo? = null,
    val inclination: EndingInclination? = null
): JsonSerializable {
    companion object: JsonSerializer<PitchInfo> {
        override fun fromJson(json: JSONObject): PitchInfo = PitchInfo(
            json.getUInt("pitch"),
            json.getStringOrNull("grade")?.let { GradeValue.fromString(it) },
            json.getUIntOrNull("height"),
            json.getEnumOrNull(Ending::class, "ending"),
            json.getEnumOrNull(EndingInfo::class, "info"),
            json.getEnumOrNull(EndingInclination::class, "inclination")
        )
    }

    override fun toJson(): JSONObject = jsonOf(
        "pitch" to pitch,
        "grade" to gradeValue?.name,
        "height" to height,
        "ending" to ending,
        "info" to info,
        "inclination" to inclination
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PitchInfo

        if (pitch != other.pitch) return false
        if (gradeValue != other.gradeValue) return false
        if (height != other.height) return false
        if (ending != other.ending) return false
        if (info != other.info) return false
        if (inclination != other.inclination) return false

        return true
    }

    override fun hashCode(): Int {
        var result = pitch.hashCode()
        result = 31 * result + (gradeValue?.hashCode() ?: 0)
        result = 31 * result + (height?.hashCode() ?: 0)
        result = 31 * result + (ending?.hashCode() ?: 0)
        result = 31 * result + (info?.hashCode() ?: 0)
        result = 31 * result + (inclination?.hashCode() ?: 0)
        return result
    }
}
