package data

import database.serialization.Json
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString

/**
 * Represents a data point with location, label, and icon information.
 *
 * @property location The location of the data point.
 * @property label The label associated with the data point.
 * @property icon The icon associated with the data point.
 * @property description An extra optional text to explain more about the point.
 */
@Serializable
data class DataPoint(
    val location: LatLng,
    val label: String,
    val icon: String,
    val description: String? = null,
) {
    override fun toString(): String = Json.encodeToString(this)
}
