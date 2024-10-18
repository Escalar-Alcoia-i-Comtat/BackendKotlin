package data

import database.serialization.Json
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString

@Serializable
data class PitchInfo(
    val pitch: UInt,
    val grade: Grade? = null,
    val height: UInt? = null,
    val ending: Ending? = null,
    val info: EndingInfo? = null,
    val inclination: EndingInclination? = null
) {
    override fun toString(): String = Json.encodeToString(this)
}
