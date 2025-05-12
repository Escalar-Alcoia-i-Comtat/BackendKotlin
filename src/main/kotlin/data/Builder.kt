package data

import database.serialization.Json
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString

@Serializable
data class Builder(
    val name: String? = null,
    val date: String? = null,
) {
    override fun toString(): String = Json.encodeToString(this)
}
