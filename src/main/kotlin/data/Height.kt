package data

import KoverIgnore
import database.serialization.Json
import kotlinx.serialization.encodeToString

@KoverIgnore
data class Height(
    val pitch: Int,
    val height: Int
) {
    override fun toString(): String = Json.encodeToString(this)
}
