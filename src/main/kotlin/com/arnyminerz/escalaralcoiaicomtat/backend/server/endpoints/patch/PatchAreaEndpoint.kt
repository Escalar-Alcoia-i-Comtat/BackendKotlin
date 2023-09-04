package com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.patch

import com.arnyminerz.escalaralcoiaicomtat.backend.ServerDatabase
import com.arnyminerz.escalaralcoiaicomtat.backend.database.entity.Area
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.SecureEndpointBase
import com.arnyminerz.escalaralcoiaicomtat.backend.server.error.Errors
import com.arnyminerz.escalaralcoiaicomtat.backend.server.request.save
import com.arnyminerz.escalaralcoiaicomtat.backend.server.response.respondFailure
import com.arnyminerz.escalaralcoiaicomtat.backend.server.response.respondSuccess
import com.arnyminerz.escalaralcoiaicomtat.backend.storage.Storage
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.jsonOf
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.request.receiveMultipart
import io.ktor.server.util.getValue
import io.ktor.util.pipeline.PipelineContext
import java.io.File
import java.net.URL
import java.time.Instant
import java.util.UUID

object PatchAreaEndpoint : SecureEndpointBase() {
    override suspend fun PipelineContext<Unit, ApplicationCall>.endpoint() {
        val areaId: Int by call.parameters

        val area = ServerDatabase.instance.query { Area.findById(areaId) }
            ?: return respondFailure(Errors.ObjectNotFound)

        val multipart = call.receiveMultipart()

        var displayName: String? = null
        var webUrl: String? = null

        var imageFile: File? = null

        multipart.forEachPart { partData ->
            when (partData) {
                is PartData.FormItem -> {
                    when (partData.name) {
                        "displayName" -> displayName = partData.value
                        "webUrl" -> webUrl = partData.value
                    }
                }
                is PartData.FileItem -> {
                    when (partData.name) {
                        "image" -> {
                            val uuid = ServerDatabase.instance.query { area.image.nameWithoutExtension }
                            imageFile = partData.save(Storage.ImagesDir, UUID.fromString(uuid))
                        }
                    }
                }
                else -> Unit
            }
        }

        if (displayName == null && webUrl == null && imageFile == null) {
            return respondSuccess(httpStatusCode = HttpStatusCode.NoContent)
        }

        val json = ServerDatabase.instance.query {
            displayName?.let { area.displayName = it }
            webUrl?.let { area.webUrl = URL(it) }

            area.timestamp = Instant.now()

            area.toJson()
        }

        respondSuccess(
            data = jsonOf("element" to json)
        )
    }
}
