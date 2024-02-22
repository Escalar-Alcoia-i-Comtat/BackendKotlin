package server.endpoints.query

import ServerDatabase
import database.entity.Area
import database.entity.Path
import database.entity.Sector
import database.entity.Zone
import io.ktor.server.application.ApplicationCall
import io.ktor.util.pipeline.PipelineContext
import server.endpoints.EndpointBase
import server.response.respondSuccess
import utils.jsonOf
import utils.mapJson

object TreeEndpoint : EndpointBase("/tree") {
    override suspend fun PipelineContext<Unit, ApplicationCall>.endpoint() {
        val array = ServerDatabase.instance.query {
            val zones = Zone.all()
            val sectors = Sector.all()
            val paths = Path.all()

            Area.all().mapJson { it.toJsonWithZones(zones, sectors, paths) }
        }
        respondSuccess(
            jsonOf("areas" to array)
        )
    }
}
