package com.arnyminerz.escalaralcoiaicomtat.backend.utils

import com.arnyminerz.escalaralcoiaicomtat.backend.assertions.assertContentEquals
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import org.json.JSONArray
import org.json.JSONObject

class JsonUtilsTest {
    @Test
    fun `test String_json`() {
        val testJson = "{\"test\":\"value\"}"
        assertEquals(testJson, testJson.json.toString())
    }

    @Test
    fun `test putAll`() {
        val json = JSONObject()
        json.putAll(
            mapOf(
                "boolean" to true,
                "double" to 3.76,
                "integer" to 9,
                "array" to JSONArray(),
                "object" to JSONObject(),
                "long" to 5L,
                "string" to "abc",
                "null" to JSONObject.NULL
            )
        )
        assertEquals(8, json.length())
        assertEquals(true, json.getBoolean("boolean"))
        assertEquals(3.76, json.getDouble("double"))
        assertEquals(9, json.getInt("integer"))
        assertContentEquals(JSONArray(), json.getJSONArray("array"))
        assertContentEquals(JSONObject(), json.getJSONObject("object"))
        assertEquals(5L, json.getLong("long"))
        assertEquals("abc", json.getString("string"))
        assertEquals(JSONObject.NULL, json.get("null"))
    }

    @Test
    fun `test jsonOf map`() {
        val json = jsonOf(mapOf("test" to "value"))
        assertEquals("value", json.getString("test"))
    }

    @Test
    fun `test jsonOf pairs`() {
        val json = jsonOf("test" to "value")
        assertEquals("value", json.getString("test"))
    }

    @Test
    fun `test JSONObject_getJSONObjectOrNull`() {
        val json = jsonOf("test" to jsonOf("key" to "value"))
        assertNull(json.getJSONObjectOrNull("null"))
        assertContentEquals(jsonOf("key" to "value"), json.getJSONObjectOrNull("test")!!)
    }

    @Test
    fun `test JSONObject_getIntOrNull`() {
        val json = jsonOf("test" to 7)
        assertNull(json.getIntOrNull("null"))
        assertEquals(7, json.getIntOrNull("test"))
    }

    @Test
    fun `test JSONObject_getLongOrNull`() {
        val json = jsonOf("test" to 123456789L)
        assertNull(json.getLongOrNull("null"))
        assertEquals(123456789L, json.getLongOrNull("test"))
    }

    @Test
    fun `test JSONObject_getUInt`() {
        val json = jsonOf("test" to 123U, "test2" to -123)
        assertEquals(123U, json.getUInt("test"))
        assertFailsWith(NumberFormatException::class) { json.getUInt("test2") }
    }

    @Test
    fun `test JSONObject_getUIntOrNull`() {
        val json = jsonOf("test" to 123U, "test2" to -123)
        assertNull(json.getUIntOrNull("null"))
        assertEquals(123U, json.getUIntOrNull("test"))
        assertNull(json.getUIntOrNull("test2"))
    }
}
