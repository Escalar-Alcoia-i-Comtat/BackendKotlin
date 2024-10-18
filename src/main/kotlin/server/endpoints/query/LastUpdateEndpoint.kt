package server.endpoints.query

import ServerDatabase
import database.entity.info.LastUpdate
import io.ktor.server.routing.RoutingContext
import server.endpoints.EndpointBase
import server.response.query.LastUpdateResponseData
import server.response.respondSuccess

object LastUpdateEndpoint : EndpointBase("/last_update") {
    override suspend fun RoutingContext.endpoint() {
        val lastUpdate = ServerDatabase.instance.query { LastUpdate.get() }

        respondSuccess(
            data = LastUpdateResponseData(lastUpdate)
        )
    }
}
