package data

import java.time.Month
import org.json.JSONObject
import utils.getUShort
import utils.jsonOf
import utils.serialization.JsonSerializable
import utils.serialization.JsonSerializer

data class BlockingRecurrenceYearly(
    val fromDay: UShort,
    val fromMonth: Month,
    val toDay: UShort,
    val toMonth: Month
): JsonSerializable {
    companion object: JsonSerializer<BlockingRecurrenceYearly> {
        override fun fromJson(json: JSONObject): BlockingRecurrenceYearly = BlockingRecurrenceYearly(
            json.getUShort("from_day"),
            json.getEnum(Month::class.java, "from_month"),
            json.getUShort("to_day"),
            json.getEnum(Month::class.java, "to_month")
        )
    }

    override fun toJson(): JSONObject = jsonOf(
        "from_day" to fromDay,
        "from_month" to fromMonth,
        "to_day" to toDay,
        "to_month" to toMonth
    )

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
