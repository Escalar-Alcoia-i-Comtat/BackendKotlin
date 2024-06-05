package diagnostics

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import system.EnvironmentVariables

class TestDiagnostics {
    @Test
    fun `test init`() {
        assertFalse { Diagnostics.init() }
        assertFalse { Diagnostics.isInitialized }

        val dsn: String? = System.getenv("SENTRY_DSN_TESTS")
        assertNotNull(dsn)
        EnvironmentVariables.Diagnostics.SentryDsn.value = dsn

        assertTrue { Diagnostics.init() }
        assertTrue { Diagnostics.isInitialized }
    }
}
