package data

import KoverIgnore
import kotlinx.serialization.Serializable

@KoverIgnore
@Serializable
data class PhoneSignalAvailability(
    val strength: PhoneSignalStrength,
    val carrier: PhoneCarrier,
)
