package com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.create

import com.arnyminerz.escalaralcoiaicomtat.backend.ServerDatabase
import com.arnyminerz.escalaralcoiaicomtat.backend.database.entity.Area
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.SecureEndpointBase
import com.arnyminerz.escalaralcoiaicomtat.backend.server.error.Errors.MissingData
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
import io.ktor.util.pipeline.PipelineContext
import java.io.File
import java.net.URL

object NewAreaEndpoint : SecureEndpointBase() {
    override suspend fun PipelineContext<Unit, ApplicationCall>.endpoint() {
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
                        "image" -> imageFile = partData.save(Storage.ImagesDir)
                    }
                }
                else -> Unit
            }
        }

        if (displayName == null || webUrl == null || imageFile == null) {
            imageFile?.delete()
            return respondFailure(
                MissingData,
                rawMultipartFormItems.toList().joinToString(", ") { (k, v) -> "$k=$v" }
            )
        }

        val area = ServerDatabase.instance.query {
            Area.new {
                this.displayName = displayName!!
                this.image = imageFile!!
                this.webUrl = URL(webUrl!!)
            }
        }

        respondSuccess(
            jsonOf("element_id" to area.id.value),
            httpStatusCode = HttpStatusCode.Created
        )
    }
}
