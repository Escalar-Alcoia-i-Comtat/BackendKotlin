package data

import KoverIgnore
import kotlinx.serialization.Serializable

@KoverIgnore
@Serializable
enum class EndingInclination {
    VERTICAL, DIAGONAL, HORIZONTAL
}
