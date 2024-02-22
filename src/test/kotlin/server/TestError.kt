package server

import kotlin.test.Test
import kotlin.test.assertEquals
import server.error.Error

class TestError {
    @Test
    fun `test Error json conversion`() {
        val error = Error(0, "Testing message")
        val json = error.toJson()
        assertEquals(0, json.getInt("code"))
        assertEquals("Testing message", json.getString("message"))
    }
}
