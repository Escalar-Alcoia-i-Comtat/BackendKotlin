package server.endpoints.query

import ServerDatabase
import database.entity.Area
import database.entity.Path
import database.entity.Sector
import database.entity.Zone
import io.ktor.server.routing.RoutingContext
import server.endpoints.EndpointBase
import server.response.query.TreeResponseData
import server.response.respondSuccess

object TreeEndpoint : EndpointBase("/tree") {
    override suspend fun RoutingContext.endpoint() {
        val array = ServerDatabase {
            val zones = Zone.all().toList()
            val sectors = Sector.all().toList()
            val paths = Path.all().toList()

            Area.all().onEach { it.populateZones(zones, sectors, paths) }
                .toList()
        }
        respondSuccess(
            data = TreeResponseData(array.toList())
        )
    }
}
