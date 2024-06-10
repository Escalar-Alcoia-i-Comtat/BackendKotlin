package server.response.query

import KoverIgnore
import kotlinx.serialization.Serializable
import server.response.ResponseData

@KoverIgnore
@Serializable
data class ServerInfoResponseData(
    val version: String,
    val uuid: String,
    val databaseVersion: Int?
): ResponseData
