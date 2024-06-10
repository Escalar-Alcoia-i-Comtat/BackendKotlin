package server.response.query

import database.entity.Area
import kotlinx.serialization.Serializable
import server.response.ResponseData

@Serializable
data class TreeResponseData(
    val areas: List<Area>
): ResponseData
