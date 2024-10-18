package server.endpoints.blocking

import ServerDatabase
import database.EntityTypes
import database.entity.Blocking
import database.entity.info.LastUpdate
import distribution.Notifier
import io.ktor.server.routing.RoutingContext
import io.ktor.server.util.getValue
import server.endpoints.SecureEndpointBase
import server.error.Errors
import server.response.respondFailure
import server.response.respondSuccess

object DeleteBlockEndpoint : SecureEndpointBase("/block/{blockId}") {
    override suspend fun RoutingContext.endpoint() {
        val blockId: Int by call.parameters

        val block = ServerDatabase.instance.query {
            Blocking.findById(blockId)
        } ?: return respondFailure(Errors.ObjectNotFound)

        ServerDatabase.instance.query {
            block.delete()
        }

        ServerDatabase.instance.query { with(LastUpdate) { set() } }

        Notifier.getInstance().notifyDeleted(EntityTypes.BLOCKING, blockId)

        respondSuccess()
    }
}
