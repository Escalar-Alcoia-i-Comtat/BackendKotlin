package data

import kotlinx.serialization.Serializable

@Serializable
data class PitchInfo(
    val pitch: UInt,
    val grade: Grade? = null,
    val height: UInt? = null,
    val ending: Ending? = null,
    val info: EndingInfo? = null,
    val inclination: EndingInclination? = null
)
