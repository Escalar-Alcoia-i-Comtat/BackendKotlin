package server.endpoints.blocking

import ServerDatabase
import database.EntityTypes
import database.entity.Blocking
import database.entity.info.LastUpdate
import distribution.Notifier
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.routing.RoutingContext
import io.ktor.server.util.getValue
import server.endpoints.EndpointBase
import server.error.Errors
import server.request.AddBlockRequest
import server.response.respondFailure
import server.response.respondSuccess
import server.response.update.UpdateResponseData
import utils.areAllNull

object PatchBlockEndpoint : EndpointBase("/block/{blockId}") {
    override suspend fun RoutingContext.endpoint() {
        val blockId: Int by call.parameters

        val block: Blocking = ServerDatabase.instance.query { Blocking.findById(blockId) }
            ?: return respondFailure(Errors.ObjectNotFound)

        val body = call.receive<AddBlockRequest>()
        val (type, recurrence, endDate) = body

        if (areAllNull(type, recurrence, endDate)) {
            return respondSuccess(httpStatusCode = HttpStatusCode.NoContent)
        }

        println("Updating block. Type=$type, recurrence=$recurrence, endDate=$endDate")

        val updatedBlock = ServerDatabase.instance.query {
            type?.let { block.type = it }
            recurrence?.let { block.recurrence = it }
            endDate?.let { block.endDate = it }

            block
        }

        ServerDatabase.instance.query { LastUpdate.set() }

        Notifier.getInstance().notifyUpdated(EntityTypes.BLOCKING, blockId)

        respondSuccess(
            UpdateResponseData(updatedBlock)
        )
    }
}
