package server.response.query

import kotlinx.serialization.Serializable
import server.response.ResponseData

@Serializable
data class ServerInfoResponseData(
    val version: String,
    val uuid: String,
    val databaseVersion: Int?
): ResponseData
