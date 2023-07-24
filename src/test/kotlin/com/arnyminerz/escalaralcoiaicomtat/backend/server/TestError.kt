package com.arnyminerz.escalaralcoiaicomtat.backend.server

import com.arnyminerz.escalaralcoiaicomtat.backend.server.error.Error
import kotlin.test.Test
import kotlin.test.assertEquals

class TestError {
    @Test
    fun `test Error json conversion`() {
        val error = Error(0, "Testing message")
        val json = error.toJson()
        assertEquals(0, json.getInt("code"))
        assertEquals("Testing message", json.getString("message"))
    }
}
