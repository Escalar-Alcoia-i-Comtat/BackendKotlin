package com.arnyminerz.escalaralcoiaicomtat.backend.data

import com.arnyminerz.escalaralcoiaicomtat.backend.utils.jsonOf
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.serialization.JsonSerializable
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.serialization.JsonSerializer
import org.json.JSONObject

/**
 * Represents a data point with location, label, and icon information.
 *
 * @property location The location of the data point.
 * @property label The label associated with the data point.
 * @property icon The icon associated with the data point.
 */
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
