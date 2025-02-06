package data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TestExternalTrack {
    private fun testValid(isLenient: Boolean) {
        val string = "Wikiloc;https://example1.com\nWikiloc;https://example2.com"
        val result = ExternalTrack.decodeFromPart(string, isLenient = isLenient)
        assertEquals(2, result.size)
        result[0].let {
            assertEquals(ExternalTrack.Type.Wikiloc, it.type)
            assertEquals("https://example1.com", it.url)
        }
        result[1].let {
            assertEquals(ExternalTrack.Type.Wikiloc, it.type)
            assertEquals("https://example2.com", it.url)
        }
    }

    @Test
    fun `test decodeFromPart lenient valid`() {
        testValid(true)
    }

    @Test
    fun `test decodeFromPart not lenient valid`() {
        testValid(false)
    }

    @Test
    fun `test decodeFromPart lenient invalid`() {
        val string = "Wikiloc;https://example1.com\nInvalid;https://example2.com"
        val result = ExternalTrack.decodeFromPart(string, isLenient = true)
        assertEquals(1, result.size)
        result[0].let {
            assertEquals(ExternalTrack.Type.Wikiloc, it.type)
            assertEquals("https://example1.com", it.url)
        }
    }

    @Test
    fun `test decodeFromPart not lenient invalid`() {
        val string = "Wikiloc;https://example1.com\nInvalid;https://example2.com"
        assertFailsWith<IllegalArgumentException> { ExternalTrack.decodeFromPart(string, isLenient = false) }
    }
}
