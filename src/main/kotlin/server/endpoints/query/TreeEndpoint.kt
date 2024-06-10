package server.endpoints.query

import ServerDatabase
import database.entity.Area
import database.entity.Path
import database.entity.Sector
import database.entity.Zone
import io.ktor.server.application.ApplicationCall
import io.ktor.util.pipeline.PipelineContext
import server.endpoints.EndpointBase
import server.response.query.TreeResponseData
import server.response.respondSuccess

object TreeEndpoint : EndpointBase("/tree") {
    override suspend fun PipelineContext<Unit, ApplicationCall>.endpoint() {
        val array = ServerDatabase.instance.query {
            val zones = Zone.all()
            val sectors = Sector.all()
            val paths = Path.all()

            Area.all().onEach { it.populateZones(zones, sectors, paths) }
        }
        respondSuccess(
            data = TreeResponseData(array.toList())
        )
    }
}
