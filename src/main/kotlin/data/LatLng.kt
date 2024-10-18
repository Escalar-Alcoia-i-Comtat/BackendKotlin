package data

import database.serialization.Json
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString

/**
 * Represents geographic coordinates in latitude and longitude.
 *
 * This data class provides a convenient way to store and manipulate latitude and longitude values.
 *
 * @property latitude The latitude value of the geographic coordinates.
 * @property longitude The longitude value of the geographic coordinates.
 *
 * @constructor Creates a new `LatLng` instance.
 *
 * @param latitude The latitude value of the geographic coordinates.
 * @param longitude The longitude value of the geographic coordinates.
 */
@Serializable
data class LatLng(
    val latitude: Double,
    val longitude: Double
) {
    override fun toString(): String = Json.encodeToString(this)
}
