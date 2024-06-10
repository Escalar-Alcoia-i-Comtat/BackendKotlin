package server.response.query

import KoverIgnore
import database.entity.Blocking
import kotlinx.serialization.Serializable
import server.response.ResponseData

@KoverIgnore
@Serializable
data class BlocksResponseData(
    val blocks: List<Blocking>
): ResponseData
