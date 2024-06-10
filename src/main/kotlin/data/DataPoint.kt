package data

import kotlinx.serialization.Serializable

/**
 * Represents a data point with location, label, and icon information.
 *
 * @property location The location of the data point.
 * @property label The label associated with the data point.
 * @property icon The icon associated with the data point.
 */
@Serializable
data class DataPoint(
    val location: LatLng,
    val label: String,
    val icon: String
)
