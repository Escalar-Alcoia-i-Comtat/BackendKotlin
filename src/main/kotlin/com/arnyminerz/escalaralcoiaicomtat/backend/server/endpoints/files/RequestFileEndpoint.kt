package com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.files

import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.EndpointBase
import com.arnyminerz.escalaralcoiaicomtat.backend.server.error.Errors
import com.arnyminerz.escalaralcoiaicomtat.backend.server.response.respondFailure
import com.arnyminerz.escalaralcoiaicomtat.backend.server.response.respondSuccess
import com.arnyminerz.escalaralcoiaicomtat.backend.storage.HashUtils
import com.arnyminerz.escalaralcoiaicomtat.backend.storage.MessageDigestAlgorithm
import com.arnyminerz.escalaralcoiaicomtat.backend.storage.Storage
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.jsonOf
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.plugins.origin
import io.ktor.server.util.getValue
import io.ktor.util.pipeline.PipelineContext
import java.nio.file.Files
import java.security.MessageDigest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object RequestFileEndpoint : EndpointBase() {
    private const val DEFAULT_HTTP_PORT = 80

    override suspend fun PipelineContext<Unit, ApplicationCall>.endpoint() {
        val uuid: String by call.parameters

        val file = Storage.find(uuid) ?: return respondFailure(Errors.FileNotFound)
        val downloadAddress = call.request.origin.let { p ->
            val port = p.serverPort.takeIf { it != DEFAULT_HTTP_PORT }?.let { ":$it" } ?: ""
            "${p.scheme}://${p.serverHost}$port/download/$uuid"
        }
        val size = withContext(Dispatchers.IO) { Files.size(file.toPath()) }

        respondSuccess(
            jsonOf(
                "hash" to HashUtils.getCheckSumFromFile(
                    MessageDigest.getInstance(MessageDigestAlgorithm.SHA_256),
                    file
                ),
                "filename" to file.name,
                "download" to downloadAddress,
                "size" to size
            )
        )
    }
}
