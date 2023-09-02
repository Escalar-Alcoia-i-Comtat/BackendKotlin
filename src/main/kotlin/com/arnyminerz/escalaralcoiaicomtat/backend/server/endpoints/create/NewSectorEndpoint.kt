package com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.create

import com.arnyminerz.escalaralcoiaicomtat.backend.ServerDatabase
import com.arnyminerz.escalaralcoiaicomtat.backend.data.LatLng
import com.arnyminerz.escalaralcoiaicomtat.backend.database.entity.Sector
import com.arnyminerz.escalaralcoiaicomtat.backend.database.entity.Zone
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.SecureEndpointBase
import com.arnyminerz.escalaralcoiaicomtat.backend.server.error.Errors.MissingData
import com.arnyminerz.escalaralcoiaicomtat.backend.server.error.Errors.ParentNotFound
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
        var weight: String? = null
        var zone: Zone? = null

        var imageFile: File? = null

        receiveMultipart(
            forEachFormItem = { partData ->
                when (partData.name) {
                    "displayName" -> displayName = partData.value
                    "point" -> point = partData.value.json.let { LatLng.fromJson(it) }
                    "kidsApt" -> kidsApt = partData.value.toBoolean()
                    "sunTime" -> sunTime = partData.value.let { Sector.SunTime.valueOf(it) }
                    "walkingTime" -> walkingTime = partData.value.toUIntOrNull()
                    "weight" -> weight = partData.value
                    "zone" -> ServerDatabase.instance.query {
                        zone = Zone.findById(partData.value.toInt())
                            ?: return@query respondFailure(ParentNotFound)
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
            return respondFailure(
                MissingData,
                jsonOf(
                    "multipart" to rawMultipartFormItems,
                    "imageFile" to imageFile?.path
                ).toString()
            )
        }

        val sector = ServerDatabase.instance.query {
            Sector.new {
                this.displayName = displayName!!
                this.point = point
                this.kidsApt = kidsApt!!
                this.sunTime = sunTime!!
                this.walkingTime = walkingTime
                this.image = imageFile!!
                weight?.let { this.weight = it }
                this.zone = zone!!
            }
        }

        respondSuccess(
            jsonOf("element" to sector.toJson()),
            httpStatusCode = HttpStatusCode.Created
        )
    }
}
