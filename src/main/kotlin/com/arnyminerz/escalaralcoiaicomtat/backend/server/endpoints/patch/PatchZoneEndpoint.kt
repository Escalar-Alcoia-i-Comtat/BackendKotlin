package com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.patch

import com.arnyminerz.escalaralcoiaicomtat.backend.ServerDatabase
import com.arnyminerz.escalaralcoiaicomtat.backend.data.DataPoint
import com.arnyminerz.escalaralcoiaicomtat.backend.data.LatLng
import com.arnyminerz.escalaralcoiaicomtat.backend.database.entity.Area
import com.arnyminerz.escalaralcoiaicomtat.backend.database.entity.Zone
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.EndpointBase
import com.arnyminerz.escalaralcoiaicomtat.backend.server.error.Errors
import com.arnyminerz.escalaralcoiaicomtat.backend.server.request.save
import com.arnyminerz.escalaralcoiaicomtat.backend.server.response.respondFailure
import com.arnyminerz.escalaralcoiaicomtat.backend.server.response.respondSuccess
import com.arnyminerz.escalaralcoiaicomtat.backend.storage.Storage
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.json
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.jsonArray
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.serialize
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.request.receiveMultipart
import io.ktor.server.util.getValue
import io.ktor.util.pipeline.PipelineContext
import java.io.File
import java.net.URL
import java.util.UUID

object PatchZoneEndpoint : EndpointBase() {
    override suspend fun PipelineContext<Unit, ApplicationCall>.endpoint() {
        val zoneId: Int by call.parameters

        val zone = ServerDatabase.instance.query { Zone.findById(zoneId) } ?: return respondFailure(Errors.ObjectNotFound)

        val multipart = call.receiveMultipart()

        var displayName: String? = null
        var webUrl: String? = null
        var point: LatLng? = null
        var points: Set<DataPoint>? = null
        var area: Area? = null

        var imageFile: File? = null
        var kmzFile: File? = null

        multipart.forEachPart { partData ->
            when (partData) {
                is PartData.FormItem -> {
                    when (partData.name) {
                        "displayName" -> displayName = partData.value
                        "webUrl" -> webUrl = partData.value
                        "point" -> point = partData.value.json.let { LatLng.fromJson(it) }
                        "points" -> points = partData.value.jsonArray.serialize(DataPoint.Companion).toSet()
                        "area" -> ServerDatabase.instance.query {
                            area = Area.findById(partData.value.toInt())
                        }
                    }
                }
                is PartData.FileItem -> {
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
                else -> Unit
            }
        }

        points?.let { p -> println("New Points: [${p.joinToString { it.toJson().toString() }}]") }

        if (displayName == null && webUrl == null && point == null && points == null && imageFile == null && kmzFile == null) {
            return respondSuccess(httpStatusCode = HttpStatusCode.NoContent)
        }

        ServerDatabase.instance.query {
            displayName?.let { zone.displayName = it }
            webUrl?.let { zone.webUrl = URL(it) }
            point?.let { zone.point = it }
            points?.let { zone.pointsSet = it.map { ps -> ps.toJson().toString() } }
            area?.let { zone.area = it }
        }

        respondSuccess()
    }
}
