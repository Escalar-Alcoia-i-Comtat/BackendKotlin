package server.response.query

import KoverIgnore
import java.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import server.response.ResponseData

@KoverIgnore
@Serializable
data class LastUpdateResponseData(
    @SerialName("last_update") val lastUpdate: Long?
) : ResponseData {
    constructor(lastUpdate: Instant?) : this(lastUpdate?.toEpochMilli())
}
