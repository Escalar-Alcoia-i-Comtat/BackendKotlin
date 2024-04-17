package server.endpoints.create

import ServerDatabase
import data.LatLng
import database.EntityTypes
import database.entity.Sector
import database.entity.Zone
import database.entity.info.LastUpdate
import distribution.DeviceNotifier
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.util.pipeline.PipelineContext
import java.io.File
import server.endpoints.SecureEndpointBase
import server.error.Errors.MissingData
import server.error.Errors.ParentNotFound
import server.request.save
import server.response.respondFailure
import server.response.respondSuccess
import storage.Storage
import utils.isAnyNull
import utils.json
import utils.jsonOf

object NewSectorEndpoint : SecureEndpointBase("/sector") {
    override suspend fun PipelineContext<Unit, ApplicationCall>.endpoint() {
        var displayName: String? = null
        var point: LatLng? = null
        var kidsApt: Boolean? = null
        var sunTime: Sector.SunTime? = null
        var walkingTime: UInt? = null
        var weight: String? = null
        var zone: Zone? = null

        var imageFile: File? = null
        var gpxFile: File? = null

        receiveMultipart(
            forEachFormItem = { partData ->
                when (partData.name) {
                    "displayName" -> displayName = partData.value
                    "point" -> point = partData.value.json.let { LatLng.fromJson(it) }
                    "kidsApt" -> kidsApt = partData.value.toBoolean()
                    "sunTime" -> sunTime = partData.value.let { Sector.SunTime.valueOf(it) }
                    "walkingTime" -> walkingTime = partData.value.toUIntOrNull()
                    "weight" -> weight = partData.value
                    "zone" -> ServerDatabase.instance.query {
                        zone = Zone.findById(partData.value.toInt()) ?: return@query respondFailure(ParentNotFound)
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

        if (isAnyNull(displayName, imageFile, kidsApt, sunTime, zone)) {
            imageFile?.delete()
            gpxFile?.delete()
            return respondFailure(
                MissingData,
                jsonOf(
                    "multipart" to rawMultipartFormItems,
                    "imageFile" to imageFile?.path,
                    "gpxFile" to gpxFile?.path
                ).toString()
            )
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
                weight?.let { this.weight = it }
                this.zone = zone!!
            }.toJson()
        }

        ServerDatabase.instance.query { LastUpdate.set() }

        DeviceNotifier.notifyCreated(EntityTypes.SECTOR, sector["id"] as Int)

        respondSuccess(
            jsonOf("element" to sector),
            httpStatusCode = HttpStatusCode.Created
        )
    }
}
