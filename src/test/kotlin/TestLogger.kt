import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TestLogger {
    private fun testLevel(
        block: (message: String) -> Unit,
        letter: Char
    ) {
        Logger.clear()
        assertFalse { Logger.collectTrace }

        assertEquals(0, Logger.trace.size, "trace should be empty. Actual: ${Logger.trace}")
        block("new message")
        assertEquals(0, Logger.trace.size, "trace should be empty. Actual: ${Logger.trace}")

        Logger.startCollect()
        assertTrue { Logger.collectTrace }
        block("trace message")
        Logger.stopCollect()
        assertFalse { Logger.collectTrace }

        assertEquals(1, Logger.trace.size, "trace should be empty. Actual: ${Logger.trace}")
        val expect = " :: $letter > trace message"
        assertTrue(
            message = "Message in trace is not correct.\nActual: ${Logger.trace[0]}\nExpected: $expect",
        ) { Logger.trace[0].endsWith(expect) }

        Logger.clear()
        assertEquals(0, Logger.trace.size, "trace should be empty. Actual: ${Logger.trace}")
    }

    @Test
    fun `test debug`() = testLevel(Logger::debug, 'D')

    @Test
    fun `test info`() = testLevel(Logger::info, 'I')

    @Test
    fun `test warning`() = testLevel(Logger::warn, 'W')

    @Test
    fun `test error`() = testLevel(Logger::error, 'E')

    @Test
    fun `test error throwable`() = testLevel({ Logger.error(it, Throwable()) }, 'E')
}
