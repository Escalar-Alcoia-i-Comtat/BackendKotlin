package com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.create

import com.arnyminerz.escalaralcoiaicomtat.backend.ServerDatabase
import com.arnyminerz.escalaralcoiaicomtat.backend.data.DataPoint
import com.arnyminerz.escalaralcoiaicomtat.backend.data.LatLng
import com.arnyminerz.escalaralcoiaicomtat.backend.database.entity.Area
import com.arnyminerz.escalaralcoiaicomtat.backend.database.entity.Zone
import com.arnyminerz.escalaralcoiaicomtat.backend.database.entity.info.LastUpdate
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.SecureEndpointBase
import com.arnyminerz.escalaralcoiaicomtat.backend.server.error.Errors
import com.arnyminerz.escalaralcoiaicomtat.backend.server.error.Errors.MissingData
import com.arnyminerz.escalaralcoiaicomtat.backend.server.request.save
import com.arnyminerz.escalaralcoiaicomtat.backend.server.response.respondFailure
import com.arnyminerz.escalaralcoiaicomtat.backend.server.response.respondSuccess
import com.arnyminerz.escalaralcoiaicomtat.backend.storage.Storage
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.isAnyNull
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.json
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.jsonArray
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.jsonOf
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.serialize
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.util.pipeline.PipelineContext
import java.io.File
import java.net.URL

object NewZoneEndpoint : SecureEndpointBase("/zone") {
    @Suppress("DuplicatedCode")
    override suspend fun PipelineContext<Unit, ApplicationCall>.endpoint() {
        var displayName: String? = null
        var webUrl: String? = null
        var point: LatLng? = null
        var points: Set<DataPoint>? = null
        var area: Area? = null

        var imageFile: File? = null
        var kmzFile: File? = null

        receiveMultipart(
            forEachFormItem = { partData ->
                when (partData.name) {
                    "displayName" -> displayName = partData.value
                    "webUrl" -> webUrl = partData.value
                    "point" -> point = partData.value.json.let { LatLng.fromJson(it) }
                    "points" -> points = partData.value.jsonArray.serialize(DataPoint.Companion).toSet()
                    "area" -> ServerDatabase.instance.query {
                        area = Area.findById(partData.value.toInt())
                            ?: return@query respondFailure(Errors.ParentNotFound)
                    }
                }
            },
            forEachFileItem = { partData ->
                when (partData.name) {
                    "image" -> imageFile = partData.save(Storage.ImagesDir)
                    "kmz" -> kmzFile = partData.save(Storage.TracksDir)
                }
            }
        )

        if (isAnyNull(displayName, webUrl, imageFile, kmzFile, area)) {
            imageFile?.delete()
            kmzFile?.delete()
            return respondFailure(
                MissingData,
                rawMultipartFormItems.toList().joinToString(", ") { (k, v) -> "$k=$v" }
            )
        }

        if (points == null) points = emptySet()

        val zone = ServerDatabase.instance.query {
            Zone.new {
                this.displayName = displayName!!
                this.webUrl = URL(webUrl!!)
                this.image = imageFile!!
                this.kmz = kmzFile!!
                this.point = point
                this.pointsSet = points!!.map { it.toJson().toString() }
                this.area = area!!
            }.toJson()
        }

        ServerDatabase.instance.query { LastUpdate.set() }

        respondSuccess(
            jsonOf("element" to zone),
            httpStatusCode = HttpStatusCode.Created
        )
    }
}
