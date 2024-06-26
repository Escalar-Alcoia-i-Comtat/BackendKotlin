package data

import KoverIgnore
import kotlinx.serialization.Serializable

@KoverIgnore
@Serializable
enum class Ending {
    NONE,
    PLATE,
    PLATE_RING,
    PLATE_LANYARD,
    PLATE_CARABINER,
    CHAIN_RING,
    CHAIN_CARABINER,
    PITON,
    LANYARD,
    WALKING,
    RAPPEL
}
