package com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.create

import com.arnyminerz.escalaralcoiaicomtat.backend.ServerDatabase
import com.arnyminerz.escalaralcoiaicomtat.backend.data.GradeValue
import com.arnyminerz.escalaralcoiaicomtat.backend.data.PitchInfo
import com.arnyminerz.escalaralcoiaicomtat.backend.database.entity.Path
import com.arnyminerz.escalaralcoiaicomtat.backend.database.entity.Sector
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.EndpointBase
import com.arnyminerz.escalaralcoiaicomtat.backend.server.error.Errors.MissingData
import com.arnyminerz.escalaralcoiaicomtat.backend.server.response.respondFailure
import com.arnyminerz.escalaralcoiaicomtat.backend.server.response.respondSuccess
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.jsonArray
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.jsonOf
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.serialize
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.request.receiveMultipart
import io.ktor.util.pipeline.PipelineContext

object NewPathEndpoint : EndpointBase() {
    override suspend fun PipelineContext<Unit, ApplicationCall>.endpoint() {
        val multipart = call.receiveMultipart()

        var displayName: String? = null
        var sketchId: UInt? = null
        var height: UInt? = null
        var grade: GradeValue? = null
        var pitches: List<PitchInfo>? = null
        var stringCount: UInt? = null
        val counts: Array<UInt?> = Array(5) { null }
        val requirements: Array<Boolean?> = Array(6) { false }
        var sector: Sector? = null

        multipart.forEachPart { partData ->
            when (partData) {
                is PartData.FormItem -> {
                    when (partData.name) {
                        "displayName" -> displayName = partData.value
                        "sketch_id" -> sketchId = partData.value.toUIntOrNull()

                        "height" -> height = partData.value.toUIntOrNull()
                        "grade" -> grade = partData.value.let { GradeValue.fromString(it) }

                        "pitches" -> pitches = partData.value.jsonArray.serialize(PitchInfo)

                        "string_count" -> stringCount = partData.value.toUIntOrNull()

                        "parabolt_count" -> counts[0] = partData.value.toUIntOrNull()
                        "buril_count" -> counts[1] = partData.value.toUIntOrNull()
                        "piton_count" -> counts[2] = partData.value.toUIntOrNull()
                        "spit_count" -> counts[3] = partData.value.toUIntOrNull()
                        "tensor_count" -> counts[4] = partData.value.toUIntOrNull()

                        "cracker_required" -> requirements[0] = partData.value.toBoolean()
                        "friend_required" -> requirements[1] = partData.value.toBoolean()
                        "lanyard_required" -> requirements[2] = partData.value.toBoolean()
                        "nail_required" -> requirements[3] = partData.value.toBoolean()
                        "piton_required" -> requirements[4] = partData.value.toBoolean()
                        "stapes_required" -> requirements[5] = partData.value.toBoolean()

                        "sector" -> ServerDatabase.instance.query {
                            sector = Sector.findById(partData.value.toInt())
                        }
                    }
                }
                else -> Unit
            }
        }

        if (displayName == null || sketchId == null || sector == null) {
            return respondFailure(MissingData)
        }

        val path = ServerDatabase.instance.query {
            Path.new {
                this.displayName = displayName!!
                this.sketchId = sketchId!!
                this.height = height
                this.grade = grade
                this.pitches = pitches
                this.stringCount = stringCount
                this.paraboltCount = counts[0]
                this.burilCount = counts[1]
                this.pitonCount = counts[2]
                this.spitCount = counts[3]
                this.tensorCount = counts[4]
                this.crackerRequired = requirements[0]!!
                this.friendRequired = requirements[1]!!
                this.lanyardRequired = requirements[2]!!
                this.nailRequired = requirements[3]!!
                this.pitonRequired = requirements[4]!!
                this.stapesRequired = requirements[5]!!
                this.sector = sector!!
            }
        }

        respondSuccess(
            jsonOf("path_id" to path.id.value),
            httpStatusCode = HttpStatusCode.Created
        )
    }
}
