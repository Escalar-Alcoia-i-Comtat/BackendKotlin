package server.endpoints.files

import io.ktor.server.response.header
import io.ktor.server.response.respondFile
import io.ktor.server.response.respondOutputStream
import io.ktor.server.routing.RoutingContext
import io.ktor.server.util.getValue
import java.nio.file.Files
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import server.endpoints.EndpointBase
import server.error.Errors
import server.response.respondFailure
import storage.Storage
import utils.ImageUtils

object DownloadFileEndpoint : EndpointBase("/download/{uuid}") {
    override suspend fun RoutingContext.endpoint() {
        val uuid: String by call.parameters

        val width = call.request.queryParameters["width"]?.toIntOrNull()
        val height = call.request.queryParameters["height"]?.toIntOrNull()

        val file = Storage.find(uuid) ?: return respondFailure(Errors.FileNotFound)

        // Add the file's MIME type to the response
        withContext(Dispatchers.IO) {
            Files.probeContentType(file.toPath())
        }?.let { contentType ->
            call.response.header("Content-Type", contentType)
        }

        if (ImageUtils.isExtensionSupported(file.extension)) {
            // File is image, resizing is supported
            if (width != null || height != null) {
                // Respond the image resized
                call.respondOutputStream {
                    ImageUtils.scale(file, width, height, this, format = file.extension)
                }
                return
            }
        }

        // No special treatment required, respond file
        call.respondFile(file)
    }
}
