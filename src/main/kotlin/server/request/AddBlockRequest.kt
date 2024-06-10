package server.request

import data.BlockingRecurrenceYearly
import data.BlockingTypes
import database.serialization.external.LocalDateTimeSerializer
import java.time.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class AddBlockRequest(
    val type: BlockingTypes? = null,
    val recurrence: BlockingRecurrenceYearly? = null,
    @Serializable(with = LocalDateTimeSerializer::class) val endDate: LocalDateTime? = null
)
