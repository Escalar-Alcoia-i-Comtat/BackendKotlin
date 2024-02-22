package server.endpoints.files

import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.plugins.origin
import io.ktor.server.util.getValue
import io.ktor.util.pipeline.PipelineContext
import java.io.FileNotFoundException
import java.nio.file.Files
import java.security.MessageDigest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import server.endpoints.EndpointBase
import server.error.Errors
import server.response.respondFailure
import server.response.respondSuccess
import storage.HashUtils
import storage.MessageDigestAlgorithm
import storage.Storage
import utils.jsonOf

object RequestFileEndpoint : EndpointBase("/file/{uuids}") {
    private const val DEFAULT_HTTP_PORT = 80

    private val digest = MessageDigest.getInstance(MessageDigestAlgorithm.SHA_256)

    private suspend fun PipelineContext<Unit, ApplicationCall>.getDataFor(uuid: String): JSONObject {
        val file = Storage.find(uuid) ?: throw FileNotFoundException("Could not find file with uuid $uuid")
        val downloadAddress = call.request.origin.let { p ->
            val port = p.serverPort.takeIf { it != DEFAULT_HTTP_PORT }?.let { ":$it" } ?: ""
            "${p.scheme}://${p.serverHost}$port/download/$uuid"
        }
        val size = withContext(Dispatchers.IO) { Files.size(file.toPath()) }

        return jsonOf(
            "uuid" to uuid,
            "hash" to HashUtils.getCheckSumFromFile(digest, file),
            "filename" to file.name,
            "download" to downloadAddress,
            "size" to size
        )
    }

    override suspend fun PipelineContext<Unit, ApplicationCall>.endpoint() {
        val uuids: String by call.parameters
        val list = uuids.split(",")

        // It's impossible that "list" has size 0
        try {
            respondSuccess(
                if (list.size <= 1) {
                    getDataFor(uuids)
                } else {
                    jsonOf(
                        "files" to list.map { getDataFor(it) }
                    )
                }
            )
        } catch (_: FileNotFoundException) {
            respondFailure(Errors.FileNotFound)
        }
    }
}
