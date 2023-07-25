package com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.create

import com.arnyminerz.escalaralcoiaicomtat.backend.ServerDatabase
import com.arnyminerz.escalaralcoiaicomtat.backend.data.LatLng
import com.arnyminerz.escalaralcoiaicomtat.backend.database.entity.Sector
import com.arnyminerz.escalaralcoiaicomtat.backend.database.entity.Zone
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.EndpointBase
import com.arnyminerz.escalaralcoiaicomtat.backend.server.error.Errors.MissingData
import com.arnyminerz.escalaralcoiaicomtat.backend.server.request.save
import com.arnyminerz.escalaralcoiaicomtat.backend.server.response.respondFailure
import com.arnyminerz.escalaralcoiaicomtat.backend.server.response.respondSuccess
import com.arnyminerz.escalaralcoiaicomtat.backend.storage.Storage
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.json
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.jsonOf
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.request.receiveMultipart
import io.ktor.util.pipeline.PipelineContext
import java.io.File

object NewSectorEndpoint : EndpointBase() {
    override suspend fun PipelineContext<Unit, ApplicationCall>.endpoint() {
        val multipart = call.receiveMultipart()

        var displayName: String? = null
        var point: LatLng? = null
        var kidsApt: Boolean? = null
        var sunTime: Sector.SunTime? = null
        var walkingTime: Int? = null
        var zone: Zone? = null

        var imageFile: File? = null

        multipart.forEachPart { partData ->
            when (partData) {
                is PartData.FormItem -> {
                    when (partData.name) {
                        "displayName" -> displayName = partData.value
                        "point" -> point = partData.value.json.let { LatLng.fromJson(it) }
                        "kids_apt" -> kidsApt = partData.value.toBoolean()
                        "sun_time" -> sunTime = partData.value.let { Sector.SunTime.valueOf(it) }
                        "walking_time" -> walkingTime = partData.value.toIntOrNull()
                        "zone" -> ServerDatabase.instance.query {
                            zone = Zone.findById(partData.value.toInt())
                        }
                    }
                }
                is PartData.FileItem -> {
                    when (partData.name) {
                        "image" -> imageFile = partData.save(Storage.ImagesDir)
                    }
                }
                else -> Unit
            }
        }

        if (displayName == null || imageFile == null || kidsApt == null || sunTime == null || zone == null) {
            imageFile?.delete()
            return respondFailure(MissingData)
        }

        val sector = ServerDatabase.instance.query {
            Sector.new {
                this.displayName = displayName!!
                this.point = point
                this.kidsApt = kidsApt!!
                this.sunTime = sunTime!!
                this.walkingTime = walkingTime
                this.image = imageFile!!
                this.zone = zone!!
            }
        }

        respondSuccess(
            jsonOf("sector_id" to sector.id.value),
            httpStatusCode = HttpStatusCode.Created
        )
    }
}
