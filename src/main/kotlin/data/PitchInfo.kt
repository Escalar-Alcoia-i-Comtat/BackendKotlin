package data

import kotlinx.serialization.Serializable
import org.json.JSONObject
import utils.getEnumOrNull
import utils.getStringOrNull
import utils.getUInt
import utils.getUIntOrNull
import utils.jsonOf
import utils.serialization.JsonSerializable
import utils.serialization.JsonSerializer

@Serializable
data class PitchInfo(
    val pitch: UInt,
    val grade: Grade? = null,
    val height: UInt? = null,
    val ending: Ending? = null,
    val info: EndingInfo? = null,
    val inclination: EndingInclination? = null
): JsonSerializable {
    companion object: JsonSerializer<PitchInfo> {
        override fun fromJson(json: JSONObject): PitchInfo = PitchInfo(
            json.getUInt("pitch"),
            json.getStringOrNull("grade")?.let { Grade.fromString(it) },
            json.getUIntOrNull("height"),
            json.getEnumOrNull(Ending::class, "ending"),
            json.getEnumOrNull(EndingInfo::class, "info"),
            json.getEnumOrNull(EndingInclination::class, "inclination")
        )
    }

    override fun toJson(): JSONObject = jsonOf(
        "pitch" to pitch,
        "grade" to grade?.name,
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
        if (grade != other.grade) return false
        if (height != other.height) return false
        if (ending != other.ending) return false
        if (info != other.info) return false
        if (inclination != other.inclination) return false

        return true
    }

    override fun hashCode(): Int {
        var result = pitch.hashCode()
        result = 31 * result + (grade?.hashCode() ?: 0)
        result = 31 * result + (height?.hashCode() ?: 0)
        result = 31 * result + (ending?.hashCode() ?: 0)
        result = 31 * result + (info?.hashCode() ?: 0)
        result = 31 * result + (inclination?.hashCode() ?: 0)
        return result
    }
}
