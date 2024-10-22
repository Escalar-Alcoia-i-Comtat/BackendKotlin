package server.plugins

import io.ktor.http.HttpHeaders
import io.ktor.http.content.EntityTagVersion
import io.ktor.server.http.content.LastModifiedVersion
import io.ktor.server.plugins.conditionalheaders.ConditionalHeadersConfig
import java.security.MessageDigest
import server.response.FileSource
import server.response.FileUUID
import storage.FileType
import storage.HashUtils
import storage.MessageDigestAlgorithm

fun ConditionalHeadersConfig.configure() {
    version { call, outgoingContent ->
        val fileUUID = call.response.headers[HttpHeaders.FileUUID]
        val fileSource = call.response.headers[HttpHeaders.FileSource]
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

        emptyList()
    }
}
