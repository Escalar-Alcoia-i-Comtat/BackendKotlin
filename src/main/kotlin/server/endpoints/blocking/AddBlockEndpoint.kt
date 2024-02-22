package server.endpoints.blocking

import ServerDatabase
import data.BlockingRecurrenceYearly
import data.BlockingTypes
import database.entity.Blocking
import database.entity.Path
import database.entity.info.LastUpdate
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.request.receiveText
import io.ktor.server.util.getValue
import io.ktor.util.pipeline.PipelineContext
import java.time.LocalDateTime
import server.endpoints.SecureEndpointBase
import server.error.Errors
import server.response.respondFailure
import server.response.respondSuccess
import utils.getEnumOrNull
import utils.getJSONObjectOrNull
import utils.getStringOrNull
import utils.json
import utils.jsonOf

object AddBlockEndpoint: SecureEndpointBase("/block/{pathId}") {
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

        ServerDatabase.instance.query { LastUpdate.set() }

        respondSuccess(
            data = jsonOf("element" to blocking),
            httpStatusCode = HttpStatusCode.Created
        )
    }
}
