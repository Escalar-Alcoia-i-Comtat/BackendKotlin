package com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.files

import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.EndpointBase
import com.arnyminerz.escalaralcoiaicomtat.backend.server.error.Errors
import com.arnyminerz.escalaralcoiaicomtat.backend.server.response.respondFailure
import com.arnyminerz.escalaralcoiaicomtat.backend.storage.Storage
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.ImageUtils
import io.ktor.http.Headers
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.response.header
import io.ktor.server.response.respondFile
import io.ktor.server.response.respondOutputStream
import io.ktor.server.util.getValue
import io.ktor.util.pipeline.PipelineContext
import java.nio.file.Files

object DownloadFileEndpoint : EndpointBase("/download/{uuid}") {
    override suspend fun PipelineContext<Unit, ApplicationCall>.endpoint() {
        val uuid: String by call.parameters

        val width = call.parameters["width"]?.toIntOrNull()
        val height = call.parameters["height"]?.toIntOrNull()

        val file = Storage.find(uuid) ?: return respondFailure(Errors.FileNotFound)

        if (listOf("png", "jpeg", "jpg").any { file.extension.equals(it, true) }) {
            // File is image, resizing is supported
            if (width != null || height != null) {
                // Respond the image resized
                call.respondOutputStream {
                    ImageUtils.scale(file, width, height, this)
                }
                return
            }
        }

        // No special treatment required, respond file
        Files.probeContentType(file.toPath())?.let { contentType ->
            call.response.header("Content-Type", contentType)
        }
        call.respondFile(file, file.name)
    }
}
