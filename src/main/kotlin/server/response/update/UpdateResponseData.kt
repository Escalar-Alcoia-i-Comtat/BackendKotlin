package server.response.update

import server.response.ResponseData

data class UpdateResponseData<DataType>(
    val element: DataType
): ResponseData
