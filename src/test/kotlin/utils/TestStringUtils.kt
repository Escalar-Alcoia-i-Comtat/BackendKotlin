package utils

import kotlin.test.Test
import kotlin.test.assertEquals

class TestStringUtils {
    @Test
    fun `test removeAccents`() {
        assertEquals("aaaaaeeeeeiiiiiooooouuuuuñ", "aàáäâeèéëêiíìïîoòóöôoòóöôuùúüûñ".removeAccents())
    }

    @Test
    fun `test urlEncoded`() {
        assertEquals("testing-encoding%20f%C3%B2r_urls", "testing-encoding fòr_urls".urlEncoded)
    }
}
