package com.arnyminerz.escalaralcoiaicomtat.backend.utils

import com.arnyminerz.escalaralcoiaicomtat.backend.assertions.assertContentEquals
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.serialization.JsonSerializable
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.serialization.JsonSerializer
import kotlin.test.Test
import kotlin.test.assertEquals
import org.json.JSONArray
import org.json.JSONObject

class JsonArrayUtilsTest {
    class TestSerializable(val value: String): JsonSerializable {
        companion object: JsonSerializer<TestSerializable> {
            override fun fromJson(json: JSONObject): TestSerializable = TestSerializable(
                json.getString("test")
            )
        }

        override fun toJson(): JSONObject = JSONObject().apply {
            put("test", value)
        }
    }

    @Test
    fun `test String_jsonArray`() {
        val testJson = "[1,2,3]".jsonArray
        assertEquals(3, testJson.length())
        assertEquals(1, testJson.getInt(0))
        assertEquals(2, testJson.getInt(1))
        assertEquals(3, testJson.getInt(2))
    }

    @Test
    fun `test Iterable_toJson`() {
        val list = listOf(
            TestSerializable("test1"),
            TestSerializable("test2")
        )
        val json = list.toJson()
        assertEquals(2, json.length())
        assertContentEquals("{\"test\":\"test1\"}".json, list[0].toJson())
        assertContentEquals("{\"test\":\"test2\"}".json, list[1].toJson())
    }

    @Test
    fun `test JSONArray_serialize`() {
        val array = JSONArray("[{\"test\":\"test1\"},{\"test\":\"test2\"}]")
        val serialized = array.serialize(TestSerializable.Companion)
        assertEquals(2, serialized.size)
        assertEquals("test1", serialized[0].value)
        assertEquals("test2", serialized[1].value)
    }

    @Test
    fun `test mapJson`() {
        val list = listOf(
            TestSerializable("test"),
            TestSerializable("test2")
        )
        val json = list.mapJson { JSONObject().apply { put("key", it.value) } }

        assertEquals(2, json.length())
        assertEquals("test", json.getJSONObject(0).getString("key"))
        assertEquals("test2", json.getJSONObject(1).getString("key"))
    }
}
