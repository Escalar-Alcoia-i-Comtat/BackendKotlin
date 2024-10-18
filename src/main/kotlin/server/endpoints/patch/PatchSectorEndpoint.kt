package server.endpoints.patch

import ServerDatabase
import data.LatLng
import database.EntityTypes
import database.entity.Sector
import database.entity.Zone
import database.entity.info.LastUpdate
import database.serialization.Json
import distribution.Notifier
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.routing.RoutingContext
import io.ktor.server.util.getValue
import java.io.File
import java.time.Instant
import java.util.UUID
import server.endpoints.SecureEndpointBase
import server.error.Errors
import server.request.save
import server.response.respondFailure
import server.response.respondSuccess
import server.response.update.UpdateResponseData
import storage.Storage
import utils.areAllFalse
import utils.areAllNull

object PatchSectorEndpoint : SecureEndpointBase("/sector/{sectorId}") {
    @Suppress("DuplicatedCode", "CyclomaticComplexMethod", "LongMethod")
    override suspend fun RoutingContext.endpoint() {
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

        var deleteGpx = false

        var invalidFile = false

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
                            point = Json.decodeFromString(value)
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
                        val contentType = partData.headers[HttpHeaders.ContentType]
                        val contentSize = partData.headers[HttpHeaders.ContentLength]?.toIntOrNull()

                        // Accept only content type application/gpx (includes application/gpx+xml)
                        if (contentType?.startsWith("application/gpx") != true) {
                            invalidFile = true
                        } else {
                            if (contentSize?.let { it <= 0 } == true) {
                                // If the size is 0, delete the gpx file
                                deleteGpx = true
                            } else {
                                val uuid = ServerDatabase.instance.query { sector.gpx?.nameWithoutExtension }
                                gpxFile = partData.save(
                                    rootDir = Storage.TracksDir,
                                    uuid = uuid?.let(UUID::fromString) ?: UUID.randomUUID()
                                )
                            }
                        }
                    }
                }
            }
        )

        if (invalidFile) return respondFailure(Errors.InvalidFileType)

        if (areAllNull(displayName, imageFile, gpxFile, kidsApt, point, sunTime, walkingTime, weight, zone) &&
            areAllFalse(removePoint, removeWalkingTime, deleteGpx)
        ) {
            return respondSuccess(httpStatusCode = HttpStatusCode.NoContent)
        }

        if (deleteGpx) sector.gpx?.delete()

        ServerDatabase.instance.query {
            displayName?.let { sector.displayName = it }
            kidsApt?.let { sector.kidsApt = it }
            sunTime?.let { sector.sunTime = it }
            point?.let { sector.point = it }
            walkingTime?.let { sector.walkingTime = it }
            weight?.let { sector.weight = it }
            zone?.let { sector.zone = it }

            if (removePoint) sector.point = null
            if (removeWalkingTime) sector.walkingTime = null

            if (deleteGpx) sector.gpx = null

            sector.timestamp = Instant.now()
        }

        ServerDatabase.instance.query { with(LastUpdate) { set() } }

        Notifier.getInstance().notifyUpdated(EntityTypes.SECTOR, sectorId)

        respondSuccess(
            data = UpdateResponseData(sector)
        )
    }
}
