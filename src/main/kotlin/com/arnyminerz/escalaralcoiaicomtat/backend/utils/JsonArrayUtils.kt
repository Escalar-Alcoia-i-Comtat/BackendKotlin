package com.arnyminerz.escalaralcoiaicomtat.backend.utils

import com.arnyminerz.escalaralcoiaicomtat.backend.utils.serialization.JsonSerializable
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.serialization.JsonSerializer
import org.json.JSONArray
import org.json.JSONObject


/**
 * Parses the string as a JSON array and returns the result as a JSONArray object.
 *
 * @receiver The string to be parsed as JSON.
 *
 * @return The parsed JSON array represented as a JSONArray object.
 */
val String.jsonArray: JSONArray get() = JSONArray(this)

/**
 * Converts an Iterable of JsonSerializable objects to a JSONArray.
 *
 * @return The JSONArray containing JSON representations of the JsonSerializable objects.
 */
fun Iterable<JsonSerializable>.toJson(): JSONArray = JSONArray().apply {
    for (item in this@toJson)
        put(item.toJson())
}

/**
 * Serialize a JSONArray using a provided JsonSerializer and return a List of the serialized objects.
 *
 * @param serializer The JsonSerializer used to serialize each object in the JSONArray.
 *
 * @return A List of serialized objects.
 */
fun <Result> JSONArray.serialize(serializer: JsonSerializer<Result>): List<Result> = (0 until length()).map { i ->
    val json = getJSONObject(i)
    serializer.fromJson(json)
}

/**
 * Maps each element of the iterable to a JSON object using the specified block.
 * Returns a JSON array containing the mapped objects.
 *
 * @param block the transformation block that converts an element of the iterable to a JSONObject
 *
 * @return a JSON array containing the mapped objects
 */
fun <T: Any> Iterable<T>.mapJson(block: (T) -> JSONObject): JSONArray {
    val array = JSONArray()
    for (i in 0 until count()) {
        val entry = block(elementAt(i))
        array.put(entry)
    }
    return array
}
