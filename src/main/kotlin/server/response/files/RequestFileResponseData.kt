package server.response.files

import kotlinx.serialization.Serializable
import server.response.ResponseData

@Serializable
data class RequestFileResponseData(
    val files: List<Data>
): ResponseData {
    @Serializable
    data class Data(
        val uuid: String,
        val hash: String,
        val filename: String,
        val download: String,
        val size: Long
    )
}
