package data

import KoverIgnore
import kotlinx.serialization.Serializable

@KoverIgnore
@Serializable
enum class PhoneSignalStrength {
    NOT_AVAILABLE,
    BAD_SIGNAL,
    SIGNAL_3G,
    SIGNAL_4G,
    SIGNAL_5G,
}
