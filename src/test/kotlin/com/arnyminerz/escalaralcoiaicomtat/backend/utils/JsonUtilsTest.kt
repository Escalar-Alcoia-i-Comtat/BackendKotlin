package com.arnyminerz.escalaralcoiaicomtat.backend.utils

import com.arnyminerz.escalaralcoiaicomtat.backend.assertions.assertContentEquals
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.serialization.JsonSerializable
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.serialization.JsonSerializer
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import org.json.JSONArray
import org.json.JSONObject

class JsonUtilsTest {
    object Assistance {
        @Suppress("unused")
        enum class TestEnum {
            ABC, DEF, GHI
        }

        data class Serializable(val value: String): JsonSerializable {
            companion object: JsonSerializer<Serializable> {
                override fun fromJson(json: JSONObject): Serializable = Serializable(json.getString("test"))
            }

            override fun toJson(): JSONObject = jsonOf("test" to value)
        }
    }

    @Test
    fun `test String_json`() {
        val testJson = "{\"test\":\"value\"}"
        assertEquals(testJson, testJson.json.toString())
    }

    @Test
    fun `test putAll`() {
        val json = JSONObject()
        val instant = Instant.now()
        json.putAll(
            mapOf(
                "boolean" to true,
                "double" to 3.76,
                "integer" to 9,
                "array" to JSONArray().apply { put(123) },
                "object" to JSONObject(),
                "long" to 5L,
                "string" to "abc",
                "null" to JSONObject.NULL,
                "instant" to instant,
                "list" to listOf(Assistance.Serializable("value"))
            )
        )
        assertEquals(10, json.length())
        assertEquals(true, json.getBoolean("boolean"))
        assertEquals(3.76, json.getDouble("double"))
        assertEquals(9, json.getInt("integer"))
        json.getJSONArray("array").let { arr ->
            assertEquals(1, arr.length())
            assertEquals(123, arr.getInt(0))
        }
        assertContentEquals(JSONObject(), json.getJSONObject("object"))
        assertEquals(5L, json.getLong("long"))
        assertEquals("abc", json.getString("string"))
        assertEquals(JSONObject.NULL, json.get("null"))
        assertEquals(instant.toEpochMilli(), json.getLong("instant"))
        json.getJSONArray("list").also { println("List: $it") }.serialize(Assistance.Serializable).let { list ->
            assertEquals(1, list.size)
            assertEquals("value", list[0].value)
        }
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

    @Test
    fun `test JSONObject_getEnumOrNull`() {
        val json = jsonOf("test" to Assistance.TestEnum.ABC)
        assertNull(json.getEnumOrNull(Assistance.TestEnum::class, "null"))
        assertEquals(Assistance.TestEnum.ABC, json.getEnumOrNull(Assistance.TestEnum::class, "test"))
    }

    @Test
    fun `test JSONObject_getBooleanOrNull`() {
        val json = jsonOf("test" to 0, "test2" to 1, "test3" to true, "test4" to "true", "test5" to "invalid")
        assertNull(json.getBooleanOrNull("null"))
        assertNull(json.getBooleanOrNull("test"))
        assertNull(json.getBooleanOrNull("test2"))
        assertEquals(true, json.getBooleanOrNull("test3"))
        assertEquals(true, json.getBooleanOrNull("test4"))
        assertNull(json.getBooleanOrNull("test5"))
    }

    @Test
    fun `test JSONObject_getUShort`() {
        val json = jsonOf("test" to 0, "test2" to -123, "test3" to UInt.MAX_VALUE)
        assertEquals(0U, json.getUShort("test"))
        assertFailsWith(NumberFormatException::class) { json.getUShort("test2") }
        assertFailsWith(NumberFormatException::class) { json.getUShort("test3") }
    }

    @Test
    fun `test JSONObject_getUShortOrNull`() {
        val json = jsonOf("test" to 0, "test2" to -123, "test3" to UInt.MAX_VALUE)
        assertEquals(0U, json.getUShortOrNull("test"))
        assertNull(json.getUShortOrNull("test2"))
        assertNull(json.getUShortOrNull("test3"))
        assertNull(json.getUShortOrNull("null"))
    }
}
