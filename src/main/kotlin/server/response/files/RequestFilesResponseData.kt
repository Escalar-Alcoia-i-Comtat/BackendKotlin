package server.response.files

import KoverIgnore
import kotlinx.serialization.Serializable
import server.response.ResponseData

@KoverIgnore
@Serializable
data class RequestFilesResponseData(
    val files: List<Data>
): ResponseData {
    @KoverIgnore
    @Serializable
    data class Data(
        val uuid: String,
        val hash: String,
        val filename: String,
        val download: String,
        val size: Long
    )
}
