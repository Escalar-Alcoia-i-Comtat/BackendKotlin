package server.response.query

import KoverIgnore
import database.entity.Area
import kotlinx.serialization.Serializable
import server.response.ResponseData

@KoverIgnore
@Serializable
data class TreeResponseData(
    val areas: List<Area>
): ResponseData
