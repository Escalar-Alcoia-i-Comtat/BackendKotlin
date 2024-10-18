package server.endpoints.create

import ServerDatabase
import data.DataPoint
import data.LatLng
import database.EntityTypes
import database.entity.Area
import database.entity.Zone
import database.entity.info.LastUpdate
import database.serialization.Json
import distribution.Notifier
import io.ktor.http.HttpStatusCode
import io.ktor.server.routing.RoutingContext
import java.io.File
import java.net.URL
import server.endpoints.SecureEndpointBase
import server.error.Errors
import server.error.Errors.MissingData
import server.request.save
import server.response.respondFailure
import server.response.respondSuccess
import server.response.update.UpdateResponseData
import storage.Storage
import utils.isAnyNull

object NewZoneEndpoint : SecureEndpointBase("/zone") {
    @Suppress("DuplicatedCode")
    override suspend fun RoutingContext.endpoint() {
        var displayName: String? = null
        var webUrl: String? = null
        var point: LatLng? = null
        var points: List<DataPoint>? = null
        var area: Area? = null

        // TODO: Validate webUrl

        var imageFile: File? = null
        var kmzFile: File? = null

        receiveMultipart(
            forEachFormItem = { partData ->
                when (partData.name) {
                    "displayName" -> displayName = partData.value
                    "webUrl" -> webUrl = partData.value
                    "point" -> point = Json.decodeFromString(partData.value)
                    "points" -> points = Json.decodeFromString(partData.value)
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

        if (points == null) points = emptyList()

        val zone = ServerDatabase.instance.query {
            Zone.new {
                this.displayName = displayName!!
                this.webUrl = URL(webUrl!!)
                this.image = imageFile!!
                this.kmz = kmzFile!!
                this.point = point
                this.points = points!!
                this.area = area!!
            }
        }

        ServerDatabase.instance.query { LastUpdate.set() }

        Notifier.getInstance().notifyCreated(EntityTypes.ZONE, zone.id.value)

        respondSuccess(
            UpdateResponseData(zone),
            httpStatusCode = HttpStatusCode.Created
        )
    }
}
