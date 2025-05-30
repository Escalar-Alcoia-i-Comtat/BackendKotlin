package server.request

import KoverIgnore
import kotlinx.serialization.Serializable

@KoverIgnore
@Serializable
data class AddBlogPostRequest(
    val summary: String? = null,
    val content: String? = null,
)
