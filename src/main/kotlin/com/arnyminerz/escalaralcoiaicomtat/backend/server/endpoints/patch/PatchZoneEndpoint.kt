package com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.patch

import com.arnyminerz.escalaralcoiaicomtat.backend.ServerDatabase
import com.arnyminerz.escalaralcoiaicomtat.backend.data.DataPoint
import com.arnyminerz.escalaralcoiaicomtat.backend.data.LatLng
import com.arnyminerz.escalaralcoiaicomtat.backend.database.entity.Area
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
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.jsonArray
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.serialize
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.util.getValue
import io.ktor.util.pipeline.PipelineContext
import java.io.File
import java.net.URL
import java.util.UUID

object PatchZoneEndpoint : SecureEndpointBase() {
    @Suppress("DuplicatedCode")
    override suspend fun PipelineContext<Unit, ApplicationCall>.endpoint() {
        val zoneId: Int by call.parameters

        val zone = ServerDatabase.instance.query { Zone.findById(zoneId) }
            ?: return respondFailure(Errors.ObjectNotFound)

        // Nullable types: point

        var displayName: String? = null
        var webUrl: String? = null
        var point: LatLng? = null
        var points: Set<DataPoint>? = null
        var area: Area? = null

        var removePoint = false

        var imageFile: File? = null
        var kmzFile: File? = null

        receiveMultipart(
            forEachFormItem = { partData ->
                when (partData.name) {
                    "displayName" -> displayName = partData.value
                    "webUrl" -> webUrl = partData.value
                    "points" -> points = partData.value.jsonArray.serialize(DataPoint.Companion).toSet()
                    "area" -> ServerDatabase.instance.query {
                        area = Area.findById(partData.value.toInt())
                    }
                    "point" -> partData.value.let { value ->
                        if (value == "\u0000")
                            removePoint = true
                        else
                            point = value.json.let { LatLng.fromJson(it) }
                    }
                }
            },
            forEachFileItem = { partData ->
                when (partData.name) {
                    "image" -> {
                        val uuid = ServerDatabase.instance.query { zone.image.nameWithoutExtension }
                        imageFile = partData.save(Storage.ImagesDir, UUID.fromString(uuid))
                    }
                    "kmz" -> {
                        val uuid = ServerDatabase.instance.query { zone.kmz.nameWithoutExtension }
                        kmzFile = partData.save(Storage.TracksDir, UUID.fromString(uuid))
                    }
                }
            }
        )

        points?.let { p -> println("New Points: [${p.joinToString { it.toJson().toString() }}]") }

        if (areAllNull(displayName, webUrl, point, points, imageFile, kmzFile) &&
            areAllFalse(removePoint)) {
            return respondSuccess(httpStatusCode = HttpStatusCode.NoContent)
        }

        ServerDatabase.instance.query {
            displayName?.let { zone.displayName = it }
            webUrl?.let { zone.webUrl = URL(it) }
            point?.let { zone.point = it }
            points?.let { zone.pointsSet = it.map { ps -> ps.toJson().toString() } }
            area?.let { zone.area = it }

            if (removePoint) zone.point = null
        }

        respondSuccess()
    }
}
