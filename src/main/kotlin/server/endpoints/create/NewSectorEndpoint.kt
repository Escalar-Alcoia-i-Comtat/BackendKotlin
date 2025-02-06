package server.endpoints.create

import ServerDatabase
import data.ExternalTrack
import data.LatLng
import database.EntityTypes
import database.entity.Sector
import database.entity.Zone
import database.entity.info.LastUpdate
import database.serialization.Json
import distribution.Notifier
import io.ktor.http.HttpStatusCode
import io.ktor.server.routing.RoutingContext
import java.io.File
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import server.endpoints.SecureEndpointBase
import server.error.Error
import server.error.Errors.MissingData
import server.error.Errors.ParentNotFound
import server.request.save
import server.response.respondFailure
import server.response.respondSuccess
import server.response.update.UpdateResponseData
import storage.Storage
import utils.isAnyNull

object NewSectorEndpoint : SecureEndpointBase("/sector") {
    override suspend fun RoutingContext.endpoint() {
        var displayName: String? = null
        var point: LatLng? = null
        var kidsApt: Boolean? = null
        var sunTime: Sector.SunTime? = null
        var walkingTime: UInt? = null
        var weight: String? = null
        var tracks: List<ExternalTrack>? = null
        var zone: Zone? = null

        var imageFile: File? = null
        var gpxFile: File? = null

        var error: Error? = null
        receiveMultipart(
            forEachFormItem = { partData ->
                when (partData.name) {
                    "displayName" -> displayName = partData.value
                    "point" -> point = Json.decodeFromString(partData.value)
                    "kidsApt" -> kidsApt = partData.value.toBoolean()
                    "sunTime" -> sunTime = partData.value.let { Sector.SunTime.valueOf(it) }
                    "walkingTime" -> walkingTime = partData.value.toUIntOrNull()
                    "weight" -> weight = partData.value
                    "tracks" -> tracks = ExternalTrack.decodeFromPart(partData)
                    "zone" -> ServerDatabase.instance.query {
                        zone = Zone.findById(partData.value.toInt()) ?: return@query ParentNotFound.let { error = it }
                    }
                }
            },
            forEachFileItem = { partData ->
                when (partData.name) {
                    "image" -> imageFile = partData.save(Storage.ImagesDir)
                    "gpx" -> gpxFile = partData.save(Storage.TracksDir)
                }
            }
        )
        if (error != null) {
            call.respondFailure(error)
            return
        }

        if (isAnyNull(displayName, imageFile, kidsApt, sunTime, zone)) {
            imageFile?.delete()
            gpxFile?.delete()
            return respondFailure(MissingData)
        }

        val sector = ServerDatabase.instance.query {
            Sector.new {
                this.displayName = displayName!!
                this.point = point
                this.kidsApt = kidsApt!!
                this.sunTime = sunTime!!
                this.walkingTime = walkingTime
                this.image = imageFile!!
                this.gpx = gpxFile
                this.tracks = tracks
                weight?.let { this.weight = it }
                this.zone = zone!!
            }
        }

        ServerDatabase.instance.query { LastUpdate.set() }

        Notifier.getInstance().notifyCreated(EntityTypes.SECTOR, sector.id.value)

        respondSuccess(
            data = UpdateResponseData(sector),
            httpStatusCode = HttpStatusCode.Created
        )
    }
}
