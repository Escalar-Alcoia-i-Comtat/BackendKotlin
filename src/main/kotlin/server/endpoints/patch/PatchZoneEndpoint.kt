package server.endpoints.patch

import ServerDatabase
import data.DataPoint
import data.LatLng
import database.EntityTypes
import database.entity.Area
import database.entity.Zone
import database.entity.info.LastUpdate
import database.serialization.Json
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
import utils.areAllFalse
import utils.areAllNull

object PatchZoneEndpoint : SecureEndpointBase("/zone/{zoneId}") {
    @Suppress("DuplicatedCode", "CyclomaticComplexMethod", "LongMethod")
    override suspend fun RoutingContext.endpoint() {
        val zoneId: Int by call.parameters

        val zone = ServerDatabase.instance.query { Zone.findById(zoneId) }
            ?: return respondFailure(Errors.ObjectNotFound)

        // Nullable types: point

        var displayName: String? = null
        var webUrl: String? = null
        var point: LatLng? = null
        var points: List<DataPoint>? = null
        var area: Area? = null

        var removePoint = false

        var imageFile: File? = null
        var kmzFile: File? = null

        var error: Error? = null
        receiveMultipart(
            forEachFormItem = { partData ->
                when (partData.name) {
                    "displayName" -> displayName = partData.value
                    "webUrl" -> webUrl = partData.value
                    "points" -> partData.value.let { value ->
                        if (value == "\u0000")
                            removePoint = true
                        else
                            points = Json.decodeFromString(value)
                    }
                    "area" -> ServerDatabase.instance.query {
                        area = Area.findById(partData.value.toInt())
                    }
                    "point" -> partData.value.let { value ->
                        if (value == "\u0000")
                            removePoint = true
                        else
                            point = Json.decodeFromString(value)
                    }
                }
            },
            forEachFileItem = { partData ->
                when (partData.name) {
                    "image" -> {
                        if (zone.image.exists() && !zone.image.delete()) {
                            error = Errors.CouldNotOverride
                            return@receiveMultipart
                        }
                        imageFile = partData.save(Storage.ImagesDir)
                    }
                    "kmz" -> {
                        if (zone.kmz.exists() && !zone.kmz.delete()) {
                            error = Errors.CouldNotOverride
                            return@receiveMultipart
                        }
                        kmzFile = partData.save(Storage.TracksDir)
                    }
                }
            }
        )
        if (error != null) {
            return respondFailure(error)
        }

        points?.let { println("New Points: [${Json.encodeToString(it)}]") }

        if (areAllNull(displayName, webUrl, point, points, imageFile, kmzFile) &&
            areAllFalse(removePoint)
        ) {
            return respondSuccess(httpStatusCode = HttpStatusCode.NoContent)
        }

        ServerDatabase.instance.query {
            displayName?.let { zone.displayName = it }
            webUrl?.let { zone.webUrl = URI.create(it).toURL() }
            point?.let { zone.point = it }
            points?.let { zone.points = it }
            area?.let { zone.area = it }

            imageFile?.let { zone.image = it }
            kmzFile?.let { zone.kmz = it }

            if (removePoint) zone.point = null

            zone.timestamp = Instant.now()
        }

        ServerDatabase.instance.query { LastUpdate.set() }

        Notifier.getInstance().notifyUpdated(EntityTypes.ZONE, zoneId)

        respondSuccess(
            data = UpdateResponseData(zone)
        )
    }
}
