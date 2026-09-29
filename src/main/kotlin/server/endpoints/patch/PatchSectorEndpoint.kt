package server.endpoints.patch

import Logger
import ServerDatabase
import data.ExternalTrack
import data.LatLng
import data.PhoneSignalAvailability
import org.escalaralcoiaicomtat.common.topo.Topo
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
import kotlinx.serialization.builtins.ListSerializer
import java.io.File
import java.time.Instant
import rendering.TopoRenderer
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

object PatchSectorEndpoint : SecureEndpointBase("/sector/{sectorId}") {
    @Suppress("DuplicatedCode", "CyclomaticComplexMethod", "LongMethod")
    override suspend fun RoutingContext.endpoint() {
        val sectorId: Int by call.parameters

        val sector = ServerDatabase.instance.query { Sector.findById(sectorId) }
            ?: return respondFailure(Errors.ObjectNotFound)

        // Nullable types: point, walkingTime, phoneSignalAvailability
        // Nullable files: gpxFile

        var displayName: String? = null
        var point: LatLng? = null
        var kidsApt: Boolean? = null
        var sunTime: Sector.SunTime? = null
        var walkingTime: UInt? = null
        var phoneSignalAvailability: List<PhoneSignalAvailability>? = null
        var weight: String? = null
        var tracks: List<ExternalTrack>? = null
        var zone: Zone? = null

        var removePoint = false
        var removeWalkingTime = false

        var imageFile: File? = null
        var gpxFile: File? = null

        var topo: Topo? = null
        var removeTopo = false
        var topoImageFile: File? = null

        var deleteGpx = false

        var invalidFile = false

        var error: Error? = null
        receiveMultipart(
            forEachFormItem = { partData ->
                when (partData.name) {
                    "displayName" -> displayName = partData.value
                    "kidsApt" -> kidsApt = partData.value.toBoolean()
                    "sunTime" -> sunTime = partData.value.let { Sector.SunTime.valueOf(it) }
                    "weight" -> weight = partData.value
                    "tracks" -> tracks = ExternalTrack.decodeFromPart(partData)
                    "zone" -> ServerDatabase.instance.query {
                        zone = Zone.findById(partData.value.toInt())
                            ?: return@query Errors.ParentNotFound.let { error = it }
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
                    "phoneSignalAvailability" -> phoneSignalAvailability = Json.decodeFromString(
                        ListSerializer(PhoneSignalAvailability.serializer()),
                        partData.value
                    )
                    "topo" -> partData.value.let { value ->
                        if (value == "\u0000") {
                            removeTopo = true
                        } else {
                            val decoded = runCatching { Topo.json.decodeFromString(Topo.serializer(), value) }.getOrNull()
                            if (decoded == null || decoded.validate().isNotEmpty()) {
                                error = Errors.InvalidData
                            } else {
                                topo = decoded
                            }
                        }
                    }
                }
            },
            forEachFileItem = { partData ->
                when (partData.name) {
                    "image" -> {
                        if (sector.image.exists() && !sector.image.delete()) {
                            error = Errors.CouldNotOverride
                            return@receiveMultipart
                        }
                        imageFile = partData.save(Storage.ImagesDir)
                    }
                    "topoImage" -> {
                        topoImageFile = partData.save(Storage.ImagesDir)
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
                                if (sector.gpx?.exists() == true && sector.gpx?.delete() != true) {
                                    error = Errors.CouldNotOverride
                                    return@receiveMultipart
                                }
                                gpxFile = partData.save(Storage.TracksDir)
                            }
                        }
                    }
                }
            }
        )
        if (error != null) {
            return respondFailure(error)
        }

        if (invalidFile) return respondFailure(Errors.InvalidFileType)

        // A plain image replaces a rendered topo, so both can't be sent together
        if (imageFile != null && (topo != null || topoImageFile != null)) return respondFailure(Errors.Conflict)
        // A topo needs a background to be rendered on
        if (topo != null && topoImageFile == null && ServerDatabase.instance.query { runCatching { sector.topoImage }.getOrNull() } == null) {
            return respondFailure(Errors.MissingData)
        }

        if (areAllNull(displayName, imageFile, gpxFile, kidsApt, point, sunTime, walkingTime, phoneSignalAvailability, weight, tracks, zone, topo, topoImageFile) &&
            areAllFalse(removePoint, removeWalkingTime, deleteGpx, removeTopo)
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
            phoneSignalAvailability?.let { sector.phoneSignalAvailability = it.takeUnless { it.isEmpty() } }
            weight?.let { sector.weight = it }
            tracks?.let { sector.tracks = it }
            zone?.let { sector.zone = it }

            imageFile?.let { sector.image = it }
            gpxFile?.let { sector.gpx = it }

            topo?.let { sector.topo = it }
            topoImageFile?.let { newBackground ->
                sector.topoImage?.takeIf { it != newBackground }?.delete()
                sector.topoImage = newBackground
            }
            // Removing the topo, or uploading a plain image, stops rendering. The last rendered image is kept.
            if (removeTopo || imageFile != null) {
                sector.topo = null
                sector.topoImage?.delete()
                sector.topoImage = null
            }

            if (removePoint) sector.point = null
            if (removeWalkingTime) sector.walkingTime = null

            if (deleteGpx) sector.gpx = null

            sector.timestamp = Instant.now()
        }

        ServerDatabase.instance.query { LastUpdate.set() }

        if (topo != null || topoImageFile != null) {
            try {
                TopoRenderer.renderSector(sectorId, notify = false)
            } catch (e: Exception) {
                Logger.error("Could not render the topo of sector $sectorId", e)
                return respondFailure(Errors.InvalidFileType)
            }
        }

        Notifier.getInstance().notifyUpdated(EntityTypes.SECTOR, sectorId)

        val updated = ServerDatabase.instance.query { Sector.findById(sectorId) } ?: sector
        respondSuccess(
            data = UpdateResponseData(updated)
        )
    }
}
