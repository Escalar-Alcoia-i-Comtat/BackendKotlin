package com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.blocking

import com.arnyminerz.escalaralcoiaicomtat.backend.ServerDatabase
import com.arnyminerz.escalaralcoiaicomtat.backend.data.BlockingRecurrenceYearly
import com.arnyminerz.escalaralcoiaicomtat.backend.data.BlockingTypes
import com.arnyminerz.escalaralcoiaicomtat.backend.database.entity.Blocking
import com.arnyminerz.escalaralcoiaicomtat.backend.database.entity.info.LastUpdate
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.EndpointBase
import com.arnyminerz.escalaralcoiaicomtat.backend.server.error.Errors
import com.arnyminerz.escalaralcoiaicomtat.backend.server.response.respondFailure
import com.arnyminerz.escalaralcoiaicomtat.backend.server.response.respondSuccess
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.areAllNull
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.getEnumOrNull
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.getJSONObjectOrNull
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.getStringOrNull
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.json
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.jsonOf
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.request.receiveText
import io.ktor.server.util.getValue
import io.ktor.util.pipeline.PipelineContext
import java.time.LocalDateTime

object PatchBlockEndpoint: EndpointBase() {
    override suspend fun PipelineContext<Unit, ApplicationCall>.endpoint() {
        val blockId: Int by call.parameters

        val block: Blocking = ServerDatabase.instance.query { Blocking.findById(blockId) }
            ?: return respondFailure(Errors.ObjectNotFound)

        val body = call.receiveText().json
        val type = body.getEnumOrNull(BlockingTypes::class, "type")
        val recurrence = body.getJSONObjectOrNull("recurrence")?.let(BlockingRecurrenceYearly::fromJson)
        val endDate = body.getStringOrNull("end_date")?.let(LocalDateTime::parse)

        if (areAllNull(type, recurrence, endDate)) {
            return respondSuccess(httpStatusCode = HttpStatusCode.NoContent)
        }

        println("Updating block. Type=$type, recurrence=$recurrence, endDate=$endDate")

        val updatedBlock = ServerDatabase.instance.query {
            type?.let { block.type = it }
            recurrence?.let { block.recurrence = it }
            endDate?.let { block.endDate = it }

            block.toJson()
        }

        ServerDatabase.instance.query { LastUpdate.set() }

        respondSuccess(
            jsonOf("element" to updatedBlock)
        )
    }
}
