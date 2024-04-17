package server.endpoints.blocking

import ServerDatabase
import data.BlockingRecurrenceYearly
import data.BlockingTypes
import database.EntityTypes
import database.entity.Blocking
import database.entity.info.LastUpdate
import distribution.DeviceNotifier
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.request.receiveText
import io.ktor.server.util.getValue
import io.ktor.util.pipeline.PipelineContext
import java.time.LocalDateTime
import server.endpoints.EndpointBase
import server.error.Errors
import server.response.respondFailure
import server.response.respondSuccess
import utils.areAllNull
import utils.getEnumOrNull
import utils.getJSONObjectOrNull
import utils.getStringOrNull
import utils.json
import utils.jsonOf

object PatchBlockEndpoint: EndpointBase("/block/{blockId}") {
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

        DeviceNotifier.notifyUpdated(EntityTypes.BLOCKING, blockId)

        respondSuccess(
            jsonOf("element" to updatedBlock)
        )
    }
}
