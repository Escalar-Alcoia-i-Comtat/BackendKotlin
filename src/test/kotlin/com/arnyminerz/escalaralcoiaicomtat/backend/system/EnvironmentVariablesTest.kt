package com.arnyminerz.escalaralcoiaicomtat.backend.system

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class EnvironmentVariablesTest {
    @Test
    fun `test EnvironmentVariable`() {
        assertNull(EnvironmentVariables.Testing.value)

        EnvironmentVariables.Testing.value = "testing"
        assertEquals("testing", EnvironmentVariables.Testing.value)
    }
}
