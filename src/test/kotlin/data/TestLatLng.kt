package data

import database.serialization.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlinx.serialization.encodeToString

class TestLatLng {
    @Test
    fun `test LatLng`() {
        val point = LatLng(0.12345, 0.67890)
        assertEquals(point, Json.decodeFromString(Json.encodeToString(point)))
        assertNotEquals(point, LatLng(0.12346, 0.67890))
        assertNotEquals(point, point.copy(latitude = 0.987654))
        assertNotEquals(point, point.copy(longitude = 0.987654))
        assertFalse(point.equals(""))
    }
}
