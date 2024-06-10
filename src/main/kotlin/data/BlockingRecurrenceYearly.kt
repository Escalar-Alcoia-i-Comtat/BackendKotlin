package data

import java.time.Month
import kotlinx.serialization.Serializable

@Serializable
data class BlockingRecurrenceYearly(
    val fromDay: UShort,
    val fromMonth: Month,
    val toDay: UShort,
    val toMonth: Month
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BlockingRecurrenceYearly

        if (fromDay != other.fromDay) return false
        if (fromMonth != other.fromMonth) return false
        if (toDay != other.toDay) return false
        if (toMonth != other.toMonth) return false

        return true
    }

    override fun hashCode(): Int {
        var result = fromDay.hashCode()
        result = 31 * result + fromMonth.hashCode()
        result = 31 * result + toDay.hashCode()
        result = 31 * result + toMonth.hashCode()
        return result
    }
}
