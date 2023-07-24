package com.arnyminerz.escalaralcoiaicomtat.backend.utils

import com.arnyminerz.escalaralcoiaicomtat.backend.utils.serialization.JsonSerializable
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/** Initializes a new [JSONObject] with `this` string. */
val String.json: JSONObject get() = JSONObject(this)

/**
 * Adds all key-value pairs from the given map to this [JSONObject].
 *
 * @param pairs A map containing the key-value pairs to add to this [JSONObject]. Valid values:
 * 1. [Boolean]
 * 2. [Double]
 * 3. [Integer]
 * 4. [JSONArray]
 * 5. [JSONObject]
 * 6. [Long]
 * 7. [String]
 * 8. [JSONObject.NULL]
 * 9. [JsonSerializable]
 *
 * @throws JSONException If the value is non-finite number.
 *
 * @return The updated [JSONObject], after adding the key-value pairs.
 */
fun JSONObject.putAll(pairs: Map<String, Any?>): JSONObject {
    for ((key, value) in pairs) {
        when (value) {
            is JsonSerializable -> put(key, value.toJson())
            is Iterable<*> -> if (value.none())
                put(key, JSONArray())
            else
                JSONArray().apply {
                    for (item in value) {
                        when (item) {
                            is JsonSerializable -> put(item)
                            else -> put(item)
                        }
                    }
                }
            else -> put(key, value)
        }
    }
    return this
}

/**
 * Constructs a JSON object from a map of key-value pairs.
 *
 * @param pairs a map containing the key-value pairs to be converted to a JSON object
 *
 * @return a new JSON object containing the key-value pairs from the input map
 */
fun jsonOf(pairs: Map<String, Any?>): JSONObject = JSONObject().apply {
    putAll(pairs)
}

/**
 * Creates a JSON object from the given key-value pairs.
 *
 * @param pairs The key-value pairs to be included in the JSON object.
 */
fun jsonOf(vararg pairs: Pair<String, Any?>): JSONObject = jsonOf(pairs.toMap())

/**
 * Retrieves the JSONObject value associated with the given key,
 * or returns null if the key does not exist, or the value is not a JSONObject.
 *
 * @param key the key whose associated value is to be retrieved
 *
 * @return the JSONObject value associated with the given key, or null if the key does not exist or the value is not a
 * JSONObject
 */
fun JSONObject.getJSONObjectOrNull(key: String): JSONObject? =
    try {
        if (has(key)) getJSONObject(key) else null
    } catch (e: JSONException) {
        null
    }

/**
 * Returns the integer value associated with the specified key, or null if the key is not found or the value is not an
 * integer.
 *
 * @param key the key to look up in the JSONObject
 *
 * @return the integer value associated with the key, or null if the key is not found or the value is not an integer
 */
fun JSONObject.getIntOrNull(key: String): Int? =
    try {
        if (has(key)) getInt(key) else null
    } catch (e: JSONException) {
        null
    }

/**
 * Returns the string value associated with the specified key, or null if the key is not found or the value is not a
 * string.
 *
 * @param key the key to look up in the JSONObject
 *
 * @return the string value associated with the key, or null if the key is not found or the value is not a string
 */
fun JSONObject.getStringOrNull(key: String): String? =
    try {
        if (has(key)) getString(key) else null
    } catch (e: JSONException) {
        null
    }
