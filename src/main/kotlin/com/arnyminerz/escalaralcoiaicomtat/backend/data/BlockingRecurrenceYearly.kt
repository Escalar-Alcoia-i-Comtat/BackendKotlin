package com.arnyminerz.escalaralcoiaicomtat.backend.data

import com.arnyminerz.escalaralcoiaicomtat.backend.utils.getUShort
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.jsonOf
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.serialization.JsonSerializable
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.serialization.JsonSerializer
import java.time.Month
import org.json.JSONObject

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
}
