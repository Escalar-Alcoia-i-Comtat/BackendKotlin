package server.endpoints.patch

import ServerDatabase
import database.EntityTypes
import database.entity.Area
import database.entity.info.LastUpdate
import distribution.Notifier
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.request.receiveMultipart
import io.ktor.server.routing.RoutingContext
import io.ktor.server.util.getValue
import java.io.File
import java.net.URI
import java.time.Instant
import java.util.UUID
import server.endpoints.SecureEndpointBase
import server.error.Errors
import server.request.save
import server.response.respondFailure
import server.response.respondSuccess
import server.response.update.UpdateResponseData
import storage.Storage

object PatchAreaEndpoint : SecureEndpointBase("/area/{areaId}") {
    override suspend fun RoutingContext.endpoint() {
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
            return respondSuccess(HttpStatusCode.NoContent)
        }

        ServerDatabase.instance.query {
            displayName?.let { area.displayName = it }
            webUrl?.let { area.webUrl = URI.create(it).toURL() }

            area.timestamp = Instant.now()
        }

        ServerDatabase.instance.query { with(LastUpdate) { set() } }

        Notifier.getInstance().notifyUpdated(EntityTypes.AREA, areaId)

        respondSuccess(
            data = UpdateResponseData(area)
        )
    }
}
