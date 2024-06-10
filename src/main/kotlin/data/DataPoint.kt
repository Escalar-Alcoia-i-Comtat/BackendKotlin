package data

import kotlinx.serialization.Serializable
import org.json.JSONObject
import utils.jsonOf
import utils.serialization.JsonSerializable
import utils.serialization.JsonSerializer

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
): JsonSerializable {
    companion object: JsonSerializer<DataPoint> {
        override fun fromJson(json: JSONObject): DataPoint = DataPoint(
            LatLng.fromJson(json.getJSONObject("location")),
            json.getString("label"),
            json.getString("icon")
        )
    }

    override fun toJson(): JSONObject = jsonOf(
        "location" to location,
        "label" to label,
        "icon" to icon
    )
}
