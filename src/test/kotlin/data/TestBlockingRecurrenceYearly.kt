package data

import java.time.Month
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals

class TestBlockingRecurrenceYearly {
    @Test
    fun `test equals and hashCode`() {
        val br1 = BlockingRecurrenceYearly(1U, Month.MAY, 1U, Month.AUGUST)
        val br2 = BlockingRecurrenceYearly(1U, Month.MAY, 1U, Month.AUGUST)

        assertEquals(br1, br2)
        assertFalse(br1.equals(""))
        assertNotEquals(br1, br2.copy(fromDay = 2U))
        assertNotEquals(br1, br2.copy(fromMonth = Month.AUGUST))
        assertNotEquals(br1, br2.copy(toDay = 2U))
        assertNotEquals(br1, br2.copy(toMonth = Month.SEPTEMBER))
        assertEquals(br1.hashCode(), br2.hashCode())
    }
}
