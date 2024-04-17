package server.endpoints.patch

import ServerDatabase
import data.LatLng
import database.EntityTypes
import database.entity.Sector
import database.entity.Zone
import database.entity.info.LastUpdate
import distribution.DeviceNotifier
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.util.getValue
import io.ktor.util.pipeline.PipelineContext
import java.io.File
import java.time.Instant
import java.util.UUID
import server.endpoints.SecureEndpointBase
import server.error.Errors
import server.request.save
import server.response.respondFailure
import server.response.respondSuccess
import storage.Storage
import utils.areAllFalse
import utils.areAllNull
import utils.json
import utils.jsonOf

object PatchSectorEndpoint : SecureEndpointBase("/sector/{sectorId}") {
    @Suppress("DuplicatedCode", "CyclomaticComplexMethod", "LongMethod")
    override suspend fun PipelineContext<Unit, ApplicationCall>.endpoint() {
        val sectorId: Int by call.parameters

        val sector = ServerDatabase.instance.query { Sector.findById(sectorId) }
            ?: return respondFailure(Errors.ObjectNotFound)

        // Nullable types: point, walkingTime
        // Nullable files: gpxFile

        var displayName: String? = null
        var point: LatLng? = null
        var kidsApt: Boolean? = null
        var sunTime: Sector.SunTime? = null
        var walkingTime: UInt? = null
        var weight: String? = null
        var zone: Zone? = null

        var removePoint = false
        var removeWalkingTime = false

        var imageFile: File? = null
        var gpxFile: File? = null

        receiveMultipart(
            forEachFormItem = { partData ->
                when (partData.name) {
                    "displayName" -> displayName = partData.value
                    "kidsApt" -> kidsApt = partData.value.toBoolean()
                    "sunTime" -> sunTime = partData.value.let { Sector.SunTime.valueOf(it) }
                    "weight" -> weight = partData.value
                    "zone" -> ServerDatabase.instance.query {
                        zone = Zone.findById(partData.value.toInt())
                            ?: return@query respondFailure(Errors.ParentNotFound)
                    }
                    "point" -> partData.value.let { value ->
                        if (value == "\u0000")
                            removePoint = true
                        else
                            point = value.json.let { LatLng.fromJson(it) }
                    }
                    "walkingTime" -> partData.value.let { value ->
                        if (value == "\u0000")
                            removeWalkingTime = true
                        else
                            walkingTime = value.toUIntOrNull()
                    }
                }
            },
            forEachFileItem = { partData ->
                when (partData.name) {
                    "image" -> {
                        val uuid = ServerDatabase.instance.query { sector.image.nameWithoutExtension }
                        imageFile = partData.save(Storage.ImagesDir, UUID.fromString(uuid))
                    }
                    "gpx" -> {
                        val uuid = ServerDatabase.instance.query { sector.gpx?.nameWithoutExtension }
                        gpxFile = partData.save(Storage.TracksDir, uuid?.let(UUID::fromString) ?: UUID.randomUUID())
                    }
                }
            }
        )

        if (areAllNull(displayName, imageFile, gpxFile, kidsApt, point, sunTime, walkingTime, weight, zone) &&
            areAllFalse(removePoint, removeWalkingTime)
        ) {
            return respondSuccess(httpStatusCode = HttpStatusCode.NoContent)
        }

        val json = ServerDatabase.instance.query {
            displayName?.let { sector.displayName = it }
            kidsApt?.let { sector.kidsApt = it }
            sunTime?.let { sector.sunTime = it }
            point?.let { sector.point = it }
            walkingTime?.let { sector.walkingTime = it }
            weight?.let { sector.weight = it }
            zone?.let { sector.zone = it }

            if (removePoint) sector.point = null
            if (removeWalkingTime) sector.walkingTime = null

            sector.timestamp = Instant.now()

            sector.toJson()
        }

        ServerDatabase.instance.query { LastUpdate.set() }

        DeviceNotifier.notifyUpdated(EntityTypes.SECTOR, sectorId)

        respondSuccess(
            data = jsonOf("element" to json)
        )
    }
}
