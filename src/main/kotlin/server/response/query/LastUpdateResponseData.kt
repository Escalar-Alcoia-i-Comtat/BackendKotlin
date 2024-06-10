package server.response.query

import KoverIgnore
import java.time.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import server.response.ResponseData

@KoverIgnore
@Serializable
data class LastUpdateResponseData(
    @SerialName("last_update") @Contextual val lastUpdate: Instant?
): ResponseData
