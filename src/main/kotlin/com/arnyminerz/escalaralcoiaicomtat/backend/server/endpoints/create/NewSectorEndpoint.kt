package com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.create

import com.arnyminerz.escalaralcoiaicomtat.backend.ServerDatabase
import com.arnyminerz.escalaralcoiaicomtat.backend.data.LatLng
import com.arnyminerz.escalaralcoiaicomtat.backend.database.entity.Sector
import com.arnyminerz.escalaralcoiaicomtat.backend.database.entity.Zone
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.SecureEndpointBase
import com.arnyminerz.escalaralcoiaicomtat.backend.server.error.Errors.MissingData
import com.arnyminerz.escalaralcoiaicomtat.backend.server.request.save
import com.arnyminerz.escalaralcoiaicomtat.backend.server.response.respondFailure
import com.arnyminerz.escalaralcoiaicomtat.backend.server.response.respondSuccess
import com.arnyminerz.escalaralcoiaicomtat.backend.storage.Storage
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.isAnyNull
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.json
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.jsonOf
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.util.pipeline.PipelineContext
import java.io.File

object NewSectorEndpoint : SecureEndpointBase() {
    override suspend fun PipelineContext<Unit, ApplicationCall>.endpoint() {
        var displayName: String? = null
        var point: LatLng? = null
        var kidsApt: Boolean? = null
        var sunTime: Sector.SunTime? = null
        var walkingTime: UInt? = null
        var zone: Zone? = null

        var imageFile: File? = null

        receiveMultipart(
            forEachFormItem = { partData ->
                when (partData.name) {
                    "displayName" -> displayName = partData.value
                    "point" -> point = partData.value.json.let { LatLng.fromJson(it) }
                    "kids_apt" -> kidsApt = partData.value.toBoolean()
                    "sun_time" -> sunTime = partData.value.let { Sector.SunTime.valueOf(it) }
                    "walking_time" -> walkingTime = partData.value.toUIntOrNull()
                    "zone" -> ServerDatabase.instance.query {
                        zone = Zone.findById(partData.value.toInt())
                    }
                }
            },
            forEachFileItem = { partData ->
                when (partData.name) {
                    "image" -> imageFile = partData.save(Storage.ImagesDir)
                }
            }
        )

        if (isAnyNull(displayName, imageFile, kidsApt, sunTime, zone)) {
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
