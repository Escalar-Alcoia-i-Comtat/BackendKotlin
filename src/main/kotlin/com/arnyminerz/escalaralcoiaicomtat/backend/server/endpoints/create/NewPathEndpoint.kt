package com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.create

import com.arnyminerz.escalaralcoiaicomtat.backend.ServerDatabase
import com.arnyminerz.escalaralcoiaicomtat.backend.data.Builder
import com.arnyminerz.escalaralcoiaicomtat.backend.data.GradeValue
import com.arnyminerz.escalaralcoiaicomtat.backend.data.PitchInfo
import com.arnyminerz.escalaralcoiaicomtat.backend.database.entity.Path
import com.arnyminerz.escalaralcoiaicomtat.backend.database.entity.Sector
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.SecureEndpointBase
import com.arnyminerz.escalaralcoiaicomtat.backend.server.error.Errors.MissingData
import com.arnyminerz.escalaralcoiaicomtat.backend.server.response.respondFailure
import com.arnyminerz.escalaralcoiaicomtat.backend.server.response.respondSuccess
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.json
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.jsonArray
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.jsonOf
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.serialize
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.util.pipeline.PipelineContext

object NewPathEndpoint : SecureEndpointBase() {
    /** The number of different count properties. */
    private const val COUNTS_LENGTH = 5

    /** The number of different required properties. */
    private const val REQUIREMENTS_LENGTH = 6

    private const val INDEX_COUNT_PARABOLT = 0
    private const val INDEX_COUNT_BURIL = 1
    private const val INDEX_COUNT_PITON = 2
    private const val INDEX_COUNT_SPIT = 3
    private const val INDEX_COUNT_TENSOR = 4

    private const val INDEX_REQUIRE_CRACKER = 0
    private const val INDEX_REQUIRE_FRIEND = 1
    private const val INDEX_REQUIRE_LANYARD = 2
    private const val INDEX_REQUIRE_NAIL = 3
    private const val INDEX_REQUIRE_PITON = 4
    private const val INDEX_REQUIRE_STAPES = 5

    override suspend fun PipelineContext<Unit, ApplicationCall>.endpoint() {
        var displayName: String? = null
        var sketchId: UInt? = null
        var height: UInt? = null
        var grade: GradeValue? = null
        var pitches: List<PitchInfo>? = null
        var stringCount: UInt? = null
        val counts: Array<UInt?> = Array(COUNTS_LENGTH) { null }
        val requirements: Array<Boolean?> = Array(REQUIREMENTS_LENGTH) { false }
        var showDescription: Boolean? = null
        var description: String? = null
        var builder: Builder? = null
        val reBuilder: MutableList<Builder> = mutableListOf()
        var sector: Sector? = null

        receiveMultipart(
            forEachFormItem = { partData ->
                when (partData.name) {
                    "displayName" -> displayName = partData.value
                    "sketch_id" -> sketchId = partData.value.toUIntOrNull()

                    "height" -> height = partData.value.toUIntOrNull()
                    "grade" -> grade = partData.value.let { GradeValue.fromString(it) }

                    "pitches" -> pitches = partData.value.jsonArray.serialize(PitchInfo)

                    "string_count" -> stringCount = partData.value.toUIntOrNull()

                    "parabolt_count" -> counts[INDEX_COUNT_PARABOLT] = partData.value.toUIntOrNull()
                    "buril_count" -> counts[INDEX_COUNT_BURIL] = partData.value.toUIntOrNull()
                    "piton_count" -> counts[INDEX_COUNT_PITON] = partData.value.toUIntOrNull()
                    "spit_count" -> counts[INDEX_COUNT_SPIT] = partData.value.toUIntOrNull()
                    "tensor_count" -> counts[INDEX_COUNT_TENSOR] = partData.value.toUIntOrNull()

                    "cracker_required" -> requirements[INDEX_REQUIRE_CRACKER] = partData.value.toBoolean()
                    "friend_required" -> requirements[INDEX_REQUIRE_FRIEND] = partData.value.toBoolean()
                    "lanyard_required" -> requirements[INDEX_REQUIRE_LANYARD] = partData.value.toBoolean()
                    "nail_required" -> requirements[INDEX_REQUIRE_NAIL] = partData.value.toBoolean()
                    "piton_required" -> requirements[INDEX_REQUIRE_PITON] = partData.value.toBoolean()
                    "stapes_required" -> requirements[INDEX_REQUIRE_STAPES] = partData.value.toBoolean()

                    "show_description" -> showDescription = partData.value.toBoolean()
                    "description" -> description = partData.value

                    "builder" -> builder = partData.value.let { Builder.fromJson(it.json) }
                    "re_builder" -> reBuilder.addAll(partData.value.jsonArray.serialize(Builder))

                    "sector" -> ServerDatabase.instance.query {
                        sector = Sector.findById(partData.value.toInt())
                    }
                }
            }
        )

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
                this.paraboltCount = counts[INDEX_COUNT_PARABOLT]
                this.burilCount = counts[INDEX_COUNT_BURIL]
                this.pitonCount = counts[INDEX_COUNT_PITON]
                this.spitCount = counts[INDEX_COUNT_SPIT]
                this.tensorCount = counts[INDEX_COUNT_TENSOR]
                this.crackerRequired = requirements[INDEX_REQUIRE_CRACKER]!!
                this.friendRequired = requirements[INDEX_REQUIRE_FRIEND]!!
                this.lanyardRequired = requirements[INDEX_REQUIRE_LANYARD]!!
                this.nailRequired = requirements[INDEX_REQUIRE_NAIL]!!
                this.pitonRequired = requirements[INDEX_REQUIRE_PITON]!!
                this.stapesRequired = requirements[INDEX_REQUIRE_STAPES]!!
                this.showDescription = showDescription ?: false
                this.description = description
                this.builder = builder
                this.reBuilder = reBuilder
                this.sector = sector!!
            }
        }

        respondSuccess(
            jsonOf("path_id" to path.id.value),
            httpStatusCode = HttpStatusCode.Created
        )
    }
}
