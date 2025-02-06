package assertions

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.test.assertEquals

fun assertLocalDateEquals(
    expected: LocalDateTime,
    actual: LocalDateTime?,
    message: String? = null,
    truncatedTo: ChronoUnit = ChronoUnit.SECONDS
) {
    assertEquals(expected.truncatedTo(truncatedTo), actual?.truncatedTo(truncatedTo), message)
}
