package server.response.update

import KoverIgnore
import kotlinx.serialization.Serializable
import server.response.ResponseData

@KoverIgnore
@Serializable
data class UpdateResponseData<DataType>(
    val element: DataType
): ResponseData
