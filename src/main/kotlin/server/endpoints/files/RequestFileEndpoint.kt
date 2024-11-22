package server.endpoints.files

import io.ktor.server.plugins.origin
import io.ktor.server.routing.RoutingContext
import io.ktor.server.util.getValue
import java.io.FileNotFoundException
import java.nio.file.Files
import java.security.MessageDigest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import server.endpoints.EndpointBase
import server.error.Errors
import server.response.files.RequestFileResponseData
import server.response.files.RequestFilesResponseData
import server.response.respondFailure
import server.response.respondSuccess
import storage.HashUtils
import storage.MessageDigestAlgorithm
import storage.Storage

@Deprecated("This endpoint shall be removed once the new client is deployed")
object RequestFileEndpoint : EndpointBase("/file/{uuids}") {
    private const val DEFAULT_HTTP_PORT = 80

    private val digest = MessageDigest.getInstance(MessageDigestAlgorithm.SHA_256)

    private suspend fun RoutingContext.getDataFor(uuid: String): RequestFilesResponseData.Data {
        val file = Storage.find(uuid) ?: throw FileNotFoundException("Could not find file with uuid $uuid")
        val downloadAddress = call.request.origin.let { p ->
            val port = p.serverPort.takeIf { it != DEFAULT_HTTP_PORT }?.let { ":$it" } ?: ""
            "${p.scheme}://${p.serverHost}$port/download/$uuid"
        }
        val size = withContext(Dispatchers.IO) { Files.size(file.toPath()) }

        return RequestFilesResponseData.Data(
            uuid = uuid,
            hash = HashUtils.getCheckSumFromFile(digest, file),
            filename = file.name,
            download = downloadAddress,
            size = size
        )
    }

    override suspend fun RoutingContext.endpoint() {
        val uuids: String by call.parameters
        val list = uuids.split(",")

        // It's impossible that "list" has size 0
        try {
            if (list.size <= 1) {
                respondSuccess(
                    data = RequestFileResponseData(
                        getDataFor(uuids)
                    )
                )
            } else {
                respondSuccess(
                    data = RequestFilesResponseData(
                        files = list.map { getDataFor(it) }
                    )
                )
            }
        } catch (_: FileNotFoundException) {
            respondFailure(Errors.FileNotFound)
        }
    }
}
