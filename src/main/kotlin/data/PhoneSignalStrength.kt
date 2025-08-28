package data

import KoverIgnore
import kotlinx.serialization.Serializable

@KoverIgnore
@Serializable
enum class PhoneSignalStrength {
    NOT_AVAILABLE,
    AVAILABLE,
    LOW,
}
