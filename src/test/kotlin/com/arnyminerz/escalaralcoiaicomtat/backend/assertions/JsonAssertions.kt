package com.arnyminerz.escalaralcoiaicomtat.backend.assertions

import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import org.json.JSONArray
import org.json.JSONObject

/**
 * Makes sure that the contents of both arrays are the same.
 */
fun assertContentEquals(expected: JSONArray, actual: JSONArray) {
    assertEquals(expected.length(), actual.length())
    for (i in 0 until actual.length()) {
        val actualValue = actual[i]
        val expectedValue = expected.find { it == actualValue }
        assertNotNull(expectedValue)
    }
}

/**
 * Asserts that the content of two JSONObjects is equal.
 *
 * @param expected the expected JSONObject
 * @param actual the actual JSONObject
 */
fun assertContentEquals(expected: JSONObject, actual: JSONObject) {
    assertEquals(expected.length(), actual.length())
    for (key in actual.keys()) {
        val value = actual[key]
        val expectedValue = expected[key]
        assertEquals(expectedValue, value)
    }
}
