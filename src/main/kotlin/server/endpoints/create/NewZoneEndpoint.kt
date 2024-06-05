package server.endpoints.create

import ServerDatabase
import data.DataPoint
import data.LatLng
import database.EntityTypes
import database.entity.Area
import database.entity.Zone
import database.entity.info.LastUpdate
import distribution.Notifier
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.util.pipeline.PipelineContext
import java.io.File
import java.net.URL
import server.endpoints.SecureEndpointBase
import server.error.Errors
import server.error.Errors.MissingData
import server.request.save
import server.response.respondFailure
import server.response.respondSuccess
import storage.Storage
import utils.isAnyNull
import utils.json
import utils.jsonArray
import utils.jsonOf
import utils.serialize

object NewZoneEndpoint : SecureEndpointBase("/zone") {
    @Suppress("DuplicatedCode")
    override suspend fun PipelineContext<Unit, ApplicationCall>.endpoint() {
        var displayName: String? = null
        var webUrl: String? = null
        var point: LatLng? = null
        var points: Set<DataPoint>? = null
        var area: Area? = null

        // TODO: Validate webUrl

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

        Notifier.getInstance().notifyCreated(EntityTypes.ZONE, zone["id"] as Int)

        respondSuccess(
            jsonOf("element" to zone),
            httpStatusCode = HttpStatusCode.Created
        )
    }
}
