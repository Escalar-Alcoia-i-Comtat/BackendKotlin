package server.response.files

import KoverIgnore
import kotlinx.serialization.Serializable
import server.response.ResponseData

@KoverIgnore
@Serializable
@Deprecated("This response shall be removed once the new client is deployed. See RequestFileEndpoint.")
data class RequestFileResponseData(
    val uuid: String,
    val hash: String,
    val filename: String,
    val download: String,
    val size: Long
): ResponseData {
    constructor(data: RequestFilesResponseData.Data) : this(
        data.uuid,
        data.hash,
        data.filename,
        data.download,
        data.size
    )
}
