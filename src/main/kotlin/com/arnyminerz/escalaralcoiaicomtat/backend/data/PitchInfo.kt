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
    val gradeValue: GradeValue?,
    val height: UInt?,
    val ending: Ending?,
    val info: EndingInfo?,
    val inclination: EndingInclination?
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
        "grade" to gradeValue,
        "height" to height,
        "ending" to ending,
        "info" to info,
        "inclination" to inclination
    )
}
