package data

import kotlinx.serialization.Serializable

@Serializable
data class Builder(
    val name: String?,
    val date: String?
)
