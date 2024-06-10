package data

import kotlinx.serialization.Serializable
import org.json.JSONObject
import utils.getStringOrNull
import utils.jsonOf
import utils.serialization.JsonSerializable
import utils.serialization.JsonSerializer

@Serializable
data class Builder(
    val name: String?,
    val date: String?
): JsonSerializable {
    companion object: JsonSerializer<Builder> {
        override fun fromJson(json: JSONObject): Builder = Builder(
            json.getStringOrNull("name"),
            json.getStringOrNull("date")
        )
    }

    override fun toJson(): JSONObject = jsonOf("name" to name, "date" to date)
}
