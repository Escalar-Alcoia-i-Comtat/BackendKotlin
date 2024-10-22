package server.plugins

import ServerDatabase
import database.entity.Area
import database.entity.Path
import database.entity.Sector
import database.entity.Zone
import io.ktor.http.HttpHeaders
import io.ktor.http.content.EntityTagVersion
import io.ktor.server.http.content.LastModifiedVersion
import io.ktor.server.plugins.conditionalheaders.ConditionalHeadersConfig
import java.security.MessageDigest
import server.response.FileSource
import server.response.FileUUID
import server.response.ResourceId
import server.response.ResourceType
import storage.FileType
import storage.HashUtils
import storage.MessageDigestAlgorithm

@OptIn(ExperimentalStdlibApi::class)
fun ConditionalHeadersConfig.configure() {
    version { call, outgoingContent ->
        val headers = call.response.headers

        val fileUUID = headers[HttpHeaders.FileUUID]
        val fileSource = headers[HttpHeaders.FileSource]
        val fileType = FileType.entries.find { it.headerValue == fileSource }
        if (fileUUID != null && fileType != null) {
            val file = fileType.fetcher(fileUUID)?.takeIf { it.exists() }
            if (file != null) {
                val modificationDate = file.lastModified()
                val checkSumSha256 = HashUtils.getCheckSumFromFile(
                    MessageDigest.getInstance(MessageDigestAlgorithm.SHA_256),
                    file
                )
                return@version listOf(
                    EntityTagVersion(checkSumSha256),
                    LastModifiedVersion(modificationDate)
                )
            }
        }

        val resourceType = headers[HttpHeaders.ResourceType]
        val resourceId = headers[HttpHeaders.ResourceId]?.toIntOrNull()
        if (resourceType != null && resourceId != null) {
            val resource = ServerDatabase {
                when (resourceType) {
                    "Area" -> Area[resourceId]
                    "Zone" -> Zone[resourceId]
                    "Sector" -> Sector[resourceId]
                    "Path" -> Path[resourceId]
                    else -> null
                }
            }
            if (resource != null) {
                return@version listOfNotNull(
                    EntityTagVersion(
                        ServerDatabase { resource.hashCode().toHexString() }
                    ),
                    LastModifiedVersion(resource.timestamp.toEpochMilli())
                )
            }
        }

        emptyList()
    }
}
