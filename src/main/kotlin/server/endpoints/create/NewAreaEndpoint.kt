package server.endpoints.create

import ServerDatabase
import database.EntityTypes
import database.entity.Area
import database.entity.info.LastUpdate
import distribution.Notifier
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.request.receiveMultipart
import io.ktor.util.pipeline.PipelineContext
import java.io.File
import java.net.URL
import server.endpoints.SecureEndpointBase
import server.error.Errors.MissingData
import server.request.save
import server.response.respondFailure
import server.response.respondSuccess
import server.response.update.UpdateResponseData
import storage.Storage

object NewAreaEndpoint : SecureEndpointBase("/area") {
    override suspend fun PipelineContext<Unit, ApplicationCall>.endpoint() {
        val multipart = call.receiveMultipart()

        var displayName: String? = null
        var webUrl: String? = null

        // TODO: Validate webUrl

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

        ServerDatabase.instance.query { LastUpdate.set() }

        Notifier.getInstance().notifyCreated(EntityTypes.AREA, area.id.value)

        respondSuccess(
            data = UpdateResponseData(area),
            httpStatusCode = HttpStatusCode.Created
        )
    }
}
