package server.endpoints.blocking

import ServerDatabase
import database.EntityTypes
import database.entity.Blocking
import database.entity.Path
import database.entity.info.LastUpdate
import distribution.Notifier
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.routing.RoutingContext
import io.ktor.server.util.getValue
import server.endpoints.SecureEndpointBase
import server.error.Errors
import server.request.AddBlockRequest
import server.response.respondFailure
import server.response.respondSuccess
import server.response.update.UpdateResponseData

object AddBlockEndpoint: SecureEndpointBase("/block/{pathId}") {
    override suspend fun RoutingContext.endpoint() {
        val pathId: Int by call.parameters

        val body = call.receive<AddBlockRequest>()
        val (type, recurrence, endDate) = body

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
                    this.recurrence = recurrence
                else
                    this.endDate = endDate
                this.path = path
            }
        }

        ServerDatabase.instance.query { with(LastUpdate) { set() } }

        Notifier.getInstance().notifyCreated(EntityTypes.BLOCKING, blocking.id.value)

        respondSuccess(
            data = UpdateResponseData(blocking),
            httpStatusCode = HttpStatusCode.Created
        )
    }
}
