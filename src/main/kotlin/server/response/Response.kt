package server.response

import kotlinx.serialization.Serializable

@Serializable
sealed interface Response {
    val success: Boolean
}
