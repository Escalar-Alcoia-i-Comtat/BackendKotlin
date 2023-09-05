package com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.blocking

import com.arnyminerz.escalaralcoiaicomtat.backend.ServerDatabase
import com.arnyminerz.escalaralcoiaicomtat.backend.data.BlockingRecurrenceYearly
import com.arnyminerz.escalaralcoiaicomtat.backend.data.BlockingTypes
import com.arnyminerz.escalaralcoiaicomtat.backend.database.entity.Blocking
import com.arnyminerz.escalaralcoiaicomtat.backend.database.entity.Path
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.SecureEndpointBase
import com.arnyminerz.escalaralcoiaicomtat.backend.server.error.Errors
import com.arnyminerz.escalaralcoiaicomtat.backend.server.response.respondFailure
import com.arnyminerz.escalaralcoiaicomtat.backend.server.response.respondSuccess
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

object AddBlockEndpoint: SecureEndpointBase() {
    override suspend fun PipelineContext<Unit, ApplicationCall>.endpoint() {
        val pathId: Int by call.parameters

        val body = call.receiveText().json
        val type = body.getEnumOrNull(BlockingTypes::class, "type")
        val recurrence = body.getJSONObjectOrNull("recurrence")
        val endDate = body.getStringOrNull("end_date")

        if (type == null) {
            return respondFailure(Errors.MissingData)
        } else if (recurrence != null && endDate != null) {
            return respondFailure(Errors.Conflict)
        }

        val path = ServerDatabase.instance.query {
            Path.findById(pathId)
        } ?: return respondFailure(Errors.ObjectNotFound)

        val blocking = ServerDatabase.instance.query {
            Blocking.new {
                this.type = type
                if (recurrence != null)
                    this.recurrence = BlockingRecurrenceYearly.fromJson(recurrence)
                else
                    this.endDate = endDate?.let { LocalDateTime.parse(it) }
                this.path = path
            }.toJson()
        }

        respondSuccess(
            data = jsonOf("element" to blocking),
            httpStatusCode = HttpStatusCode.Created
        )
    }
}
