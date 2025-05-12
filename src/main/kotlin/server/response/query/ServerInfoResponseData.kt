@file:UseSerializers(InstantSerializer::class)
package server.response.query

import KoverIgnore
import database.serialization.external.InstantSerializer
import java.time.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import server.response.ResponseData

@KoverIgnore
@Serializable
data class ServerInfoResponseData(
    val version: String,
    val uuid: String,
    val databaseVersion: Int?,
    val lastUpdate: Instant?,
): ResponseData
