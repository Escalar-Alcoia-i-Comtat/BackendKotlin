package server.endpoints.patch

import ServerDatabase
import data.Builder
import data.Ending
import data.Grade
import data.PitchInfo
import database.EntityTypes
import database.entity.Path
import database.entity.Sector
import database.entity.info.LastUpdate
import database.serialization.Json
import database.table.Paths
import distribution.Notifier
import io.ktor.http.HttpStatusCode
import io.ktor.server.routing.RoutingContext
import io.ktor.server.util.getValue
import java.io.File
import java.time.Instant
import localization.Localization
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

object PatchPathEndpoint : SecureEndpointBase("/path/{pathId}") {
    @Suppress("DuplicatedCode", "CyclomaticComplexMethod", "LongMethod")
    override suspend fun RoutingContext.endpoint() {
        val pathId: Int by call.parameters

        val path = ServerDatabase.instance.query { Path.findById(pathId) }
            ?: return respondFailure(Errors.ObjectNotFound)

        // Nullable types: height, grade, ending, pitches, stringCount, paraboltCount, burilCount,
        //                 pitonCount, spitCount, tensorCount, description, builder, reBuilder

        var displayName: String? = null
        var sketchId: UInt? = null
        var height: UInt? = null
        var grade: Grade? = null
        var ending: Ending? = null
        var pitches: List<PitchInfo>? = null
        var stringCount: UInt? = null
        var paraboltCount: UInt? = null
        var burilCount: UInt? = null
        var pitonCount: UInt? = null
        var spitCount: UInt? = null
        var tensorCount: UInt? = null
        var crackerRequired: Boolean? = null
        var friendRequired: Boolean? = null
        var lanyardRequired: Boolean? = null
        var nailRequired: Boolean? = null
        var pitonRequired: Boolean? = null
        var stapesRequired: Boolean? = null
        var showDescription: Boolean? = null
        var description: String? = null
        var builder: Builder? = null
        var reBuilder: List<Builder>? = null
        var imageFiles: List<File>? = null

        var sector: Sector? = null

        var removeHeight = false
        var removeGrade = false
        var removeEnding = false
        var removePitches = false
        var removeStringCount = false
        var removeParaboltCount = false
        var removeBurilCount = false
        var removePitonCount = false
        var removeSpitCount = false
        var removeTensorCount = false
        var removeDescription = false
        var removeBuilder = false
        var removeReBuilder = false
        var removeImages = emptyList<String>()

        var error: Error? = null
        receiveMultipart(
            forEachFormItem = { partData ->
                when (partData.name) {
                    "displayName" -> displayName = partData.value
                    "sketchId" -> sketchId = partData.value.toUIntOrNull()

                    "height" -> partData.value.let { value ->
                        if (value == "\u0000")
                            removeHeight = true
                        else
                            height = value.toUIntOrNull()
                    }

                    "grade" -> partData.value.let { value ->
                        if (value == "\u0000")
                            removeGrade = true
                        else
                            grade = value.let(Grade::fromString)
                    }

                    "ending" -> partData.value.let { value ->
                        if (value == "\u0000")
                            removeEnding = true
                        else
                            ending = value.let(Ending::valueOf)
                    }

                    "pitches" -> partData.value.let { value ->
                        if (value == "\u0000")
                            removePitches = true
                        else
                            pitches = Json.decodeFromString(value)
                    }

                    "stringCount" -> partData.value.let { value ->
                        if (value == "\u0000")
                            removeStringCount = true
                        else
                            stringCount = value.toUIntOrNull()
                    }

                    "paraboltCount" -> partData.value.let { value ->
                        if (value == "\u0000")
                            removeParaboltCount = true
                        else
                            paraboltCount = value.toUIntOrNull()
                    }

                    "burilCount" -> partData.value.let { value ->
                        if (value == "\u0000")
                            removeBurilCount = true
                        else
                            burilCount = value.toUIntOrNull()
                    }

                    "pitonCount" -> partData.value.let { value ->
                        if (value == "\u0000")
                            removePitonCount = true
                        else
                            pitonCount = value.toUIntOrNull()
                    }

                    "spitCount" -> partData.value.let { value ->
                        if (value == "\u0000")
                            removeSpitCount = true
                        else
                            spitCount = value.toUIntOrNull()
                    }

                    "tensorCount" -> partData.value.let { value ->
                        if (value == "\u0000")
                            removeTensorCount = true
                        else
                            tensorCount = value.toUIntOrNull()
                    }

                    "crackerRequired" -> crackerRequired = partData.value.toBoolean()
                    "friendRequired" -> friendRequired = partData.value.toBoolean()
                    "lanyardRequired" -> lanyardRequired = partData.value.toBoolean()
                    "nailRequired" -> nailRequired = partData.value.toBoolean()
                    "pitonRequired" -> pitonRequired = partData.value.toBoolean()
                    "stapesRequired" -> stapesRequired = partData.value.toBoolean()

                    "showDescription" -> showDescription = partData.value.toBoolean()
                    "description" -> partData.value.let { value ->
                        if (value == "\u0000")
                            removeDescription = true
                        else
                            description = value
                    }

                    "builder" -> partData.value.let { value ->
                        if (value == "\u0000")
                            removeBuilder = true
                        else
                            builder = Json.decodeFromString(value)
                    }

                    "reBuilder" -> partData.value.let { value ->
                        if (value == "\u0000")
                            removeReBuilder = true
                        else
                            reBuilder = Json.decodeFromString(value)
                    }

                    "removeImages" -> partData.value.let { value ->
                        removeImages = value.split('\n').filter { it.isNotBlank() }
                    }

                    "path" -> ServerDatabase.instance.query {
                        sector = Sector.findById(partData.value.toInt())
                            ?: return@query Errors.ParentNotFound.let { error = it }
                    }
                }
            },
            forEachFileItem = { partData ->
                when (partData.name) {
                    "image" -> imageFiles = (imageFiles ?: emptyList())
                        .toMutableList()
                        .apply { add(partData.save(Storage.ImagesDir)) }
                }
            }
        )
        if (error != null) {
            return respondFailure(error)
        }

        if (areAllNull(
                displayName, sketchId, height, grade, ending, pitches, stringCount, paraboltCount, burilCount,
                pitonCount, spitCount, tensorCount, crackerRequired, friendRequired, lanyardRequired, nailRequired,
                pitonRequired, stapesRequired, showDescription, description, builder, reBuilder, imageFiles, sector
            ) &&
            areAllFalse(
                removeHeight, removeGrade, removeEnding, removePitches, removeStringCount, removeParaboltCount,
                removeBurilCount, removePitonCount, removeSpitCount, removeTensorCount, removeDescription,
                removeBuilder, removeReBuilder
            ) &&
            removeImages.isEmpty()
        ) {
            return respondSuccess(httpStatusCode = HttpStatusCode.NoContent)
        }

        if (removeImages.isNotEmpty() && path.images?.isEmpty() == true) {
            // There are no images to remove
            return respondSuccess(httpStatusCode = HttpStatusCode.NoContent)
        }

        if (path.images != null && imageFiles != null && path.images!!.size + imageFiles.size > Paths.MAX_IMAGES) {
            imageFiles.forEach {
                if (!it.delete()) {
                    return respondFailure(
                        Errors.CouldNotClear,
                        "Could not remove image from invalid request: $it.\n" +
                                "Exists? ${if (it.exists()) "true" else "false"}"
                    )
                }
            }
            return respondFailure(Errors.TooManyImages)
        }

        ServerDatabase.instance.query {
            displayName?.let { path.displayName = it }
            sketchId?.let { path.sketchId = it }
            height?.let { path.height = it }
            grade?.let { path.grade = it }
            ending?.let { path.ending = it }
            pitches?.let { path.pitches = it }
            stringCount?.let { path.stringCount = it }
            paraboltCount?.let { path.paraboltCount = it }
            burilCount?.let { path.burilCount = it }
            pitonCount?.let { path.pitonCount = it }
            spitCount?.let { path.spitCount = it }
            tensorCount?.let { path.tensorCount = it }
            crackerRequired?.let { path.crackerRequired = it }
            friendRequired?.let { path.friendRequired = it }
            lanyardRequired?.let { path.lanyardRequired = it }
            nailRequired?.let { path.nailRequired = it }
            pitonRequired?.let { path.pitonRequired = it }
            stapesRequired?.let { path.stapesRequired = it }
            showDescription?.let { path.showDescription = it }
            description?.let { path.description = it }
            builder?.let { path.builder = it }
            reBuilder?.let { path.reBuilder = it }
            imageFiles?.let { path.images = it }
            sector?.let { path.sector = it }

            if (removeHeight) path.height = null
            if (removeGrade) path.grade = null
            if (removeEnding) path.ending = null
            if (removePitches) path.pitches = null
            if (removeStringCount) path.stringCount = null
            if (removeParaboltCount) path.paraboltCount = null
            if (removeBurilCount) path.burilCount = null
            if (removePitonCount) path.pitonCount = null
            if (removeSpitCount) path.spitCount = null
            if (removeTensorCount) path.tensorCount = null
            if (removeDescription) path.description = null
            if (removeBuilder) path.builder = null
            if (removeReBuilder) path.reBuilder = null

            if (removeImages.isNotEmpty()) {
                path.images = path.images?.filter { !removeImages.contains(it.name) }
            }

            path.timestamp = Instant.now()
        }

        if (path.description != null) {
            Localization.synchronizePathDescription(path)
        }

        ServerDatabase.instance.query { LastUpdate.set() }

        Notifier.getInstance().notifyUpdated(EntityTypes.PATH, pathId)

        respondSuccess(
            data = UpdateResponseData(path)
        )
    }
}
