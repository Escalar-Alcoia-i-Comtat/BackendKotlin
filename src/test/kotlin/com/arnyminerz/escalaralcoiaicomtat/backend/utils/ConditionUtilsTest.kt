package com.arnyminerz.escalaralcoiaicomtat.backend.utils

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ConditionUtilsTest {
    @Test
    fun `test areAllNull`() {
        assertTrue(areAllNull<Void>())
        assertTrue(areAllNull(null, null, null))
        assertFalse(areAllNull(true, null, null))
    }

    @Test
    fun `test isAnyNull`() {
        assertFalse(isAnyNull<Void>())
        assertTrue(isAnyNull(null, null, null))
        assertTrue(isAnyNull(true, true, null))
        assertFalse(isAnyNull(true, true, false))
    }
}
