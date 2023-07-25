package com.arnyminerz.escalaralcoiaicomtat.backend.data

import com.arnyminerz.escalaralcoiaicomtat.backend.utils.jsonOf
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.serialization.JsonSerializable
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.serialization.JsonSerializer
import org.json.JSONObject

data class Builder(
    val name: String,
    val date: String
): JsonSerializable {
    companion object: JsonSerializer<Builder> {
        override fun fromJson(json: JSONObject): Builder = Builder(
            json.getString("name"),
            json.getString("date")
        )
    }

    override fun toJson(): JSONObject = jsonOf("name" to name, "date" to date)
}
