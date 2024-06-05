package system

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EnvironmentVariablesTest {
    @Test
    fun `test EnvironmentVariable`() {
        assertNull(EnvironmentVariables.Testing.value)
        assertFalse { EnvironmentVariables.Testing.isSet }

        EnvironmentVariables.Testing.value = "testing"
        assertEquals("testing", EnvironmentVariables.Testing.value)
        assertTrue { EnvironmentVariables.Testing.isSet }
    }
}
