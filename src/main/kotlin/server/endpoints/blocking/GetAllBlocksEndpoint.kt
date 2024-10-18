package server.endpoints.blocking

import ServerDatabase
import database.entity.Blocking
import io.ktor.server.routing.RoutingContext
import server.endpoints.EndpointBase
import server.response.query.BlocksResponseData
import server.response.respondSuccess

object GetAllBlocksEndpoint : EndpointBase("/blocks") {
    override suspend fun RoutingContext.endpoint() {
        // Check that the path exists
        val blocks = ServerDatabase.instance.query { Blocking.all().toList() }

        respondSuccess(
            data = BlocksResponseData(blocks)
        )
    }
}
