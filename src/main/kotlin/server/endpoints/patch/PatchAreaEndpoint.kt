package server.endpoints.patch

import ServerDatabase
import database.EntityTypes
import database.entity.Area
import database.entity.info.LastUpdate
import distribution.Notifier
import io.ktor.http.HttpStatusCode
import io.ktor.server.routing.RoutingContext
import io.ktor.server.util.getValue
import java.io.File
import java.net.URI
import java.time.Instant
import server.endpoints.SecureEndpointBase
import server.error.Error
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

        var displayName: String? = null
        var webUrl: String? = null

        var imageFile: File? = null

        var error: Error? = null
        receiveMultipart(
            forEachFormItem = { partData ->
                when (partData.name) {
                    "displayName" -> displayName = partData.value
                    "webUrl" -> webUrl = partData.value
                }
            },
            forEachFileItem = { partData ->
                when (partData.name) {
                    "image" -> {
                        if (area.image.exists() && !area.image.delete()) {
                            error = Errors.CouldNotOverride
                            return@receiveMultipart
                        }
                        imageFile = partData.save(Storage.ImagesDir)
                    }
                }
            }
        )
        if (error != null) {
            return respondFailure(error)
        }

        if (displayName == null && webUrl == null && imageFile == null) {
            return respondSuccess(HttpStatusCode.NoContent)
        }

        ServerDatabase.instance.query {
            displayName?.let { area.displayName = it }
            webUrl?.let { area.webUrl = URI.create(it).toURL() }

            imageFile?.let { area.image = it }

            area.timestamp = Instant.now()
        }

        ServerDatabase.instance.query { LastUpdate.set() }

        Notifier.getInstance().notifyUpdated(EntityTypes.AREA, areaId)

        respondSuccess(
            data = UpdateResponseData(area)
        )
    }
}
