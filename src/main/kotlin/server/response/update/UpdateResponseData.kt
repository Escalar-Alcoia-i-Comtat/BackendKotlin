package server.response.update

import kotlinx.serialization.Serializable
import server.response.ResponseData

@Serializable
data class UpdateResponseData<DataType>(
    val element: DataType
): ResponseData
