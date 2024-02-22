package data

import kotlin.test.Test
import kotlin.test.assertEquals

class TestLatLng {
    @Test
    fun `test LatLng`() {
        val point = LatLng(0.12345, 0.67890)
        assertEquals(point, LatLng.fromJson(point.toJson()))
    }
}
