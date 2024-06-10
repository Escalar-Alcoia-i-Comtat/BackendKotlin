package server.response

import KoverIgnore
import kotlinx.serialization.Serializable

@KoverIgnore
@Serializable
sealed interface Response {
    val success: Boolean
}
