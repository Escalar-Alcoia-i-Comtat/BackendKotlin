package com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.patch

import com.arnyminerz.escalaralcoiaicomtat.backend.ServerDatabase
import com.arnyminerz.escalaralcoiaicomtat.backend.data.LatLng
import com.arnyminerz.escalaralcoiaicomtat.backend.database.entity.Sector
import com.arnyminerz.escalaralcoiaicomtat.backend.database.entity.Zone
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.SecureEndpointBase
import com.arnyminerz.escalaralcoiaicomtat.backend.server.error.Errors
import com.arnyminerz.escalaralcoiaicomtat.backend.server.request.save
import com.arnyminerz.escalaralcoiaicomtat.backend.server.response.respondFailure
import com.arnyminerz.escalaralcoiaicomtat.backend.server.response.respondSuccess
import com.arnyminerz.escalaralcoiaicomtat.backend.storage.Storage
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.areAllFalse
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.areAllNull
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.json
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.jsonOf
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.util.getValue
import io.ktor.util.pipeline.PipelineContext
import java.io.File
import java.time.Instant
import java.util.UUID

object PatchSectorEndpoint : SecureEndpointBase() {
    @Suppress("DuplicatedCode", "CyclomaticComplexMethod", "LongMethod")
    override suspend fun PipelineContext<Unit, ApplicationCall>.endpoint() {
        val sectorId: Int by call.parameters

        val sector = ServerDatabase.instance.query { Sector.findById(sectorId) }
            ?: return respondFailure(Errors.ObjectNotFound)

        // Nullable types: point, walkingTime

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
                }
            }
        )

        if (areAllNull(displayName, imageFile, kidsApt, point, sunTime, walkingTime, weight, zone) &&
            areAllFalse(removePoint, removeWalkingTime)) {
            return respondSuccess(httpStatusCode = HttpStatusCode.NoContent)
        }

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

            sector.timestamp = Instant.now()
        }

        respondSuccess(
            data = jsonOf("element" to sector.toJson())
        )
    }
}
