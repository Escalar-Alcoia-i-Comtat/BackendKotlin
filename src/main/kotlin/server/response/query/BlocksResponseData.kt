package server.response.query

import database.entity.Blocking
import kotlinx.serialization.Serializable
import server.response.ResponseData

@Serializable
data class BlocksResponseData(
    val blocks: List<Blocking>
): ResponseData
