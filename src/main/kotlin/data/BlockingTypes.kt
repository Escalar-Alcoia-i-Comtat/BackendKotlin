package data

import KoverIgnore
import kotlinx.serialization.Serializable

@KoverIgnore
@Serializable
enum class BlockingTypes {
    DRY, BUILD, BIRD, OLD, PLANTS, ROPE_LENGTH, LOOSE_ROCKS
}
