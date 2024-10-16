package server.endpoints.query

import ServerDatabase
import database.entity.Sector
import io.ktor.server.plugins.ParameterConversionException
import io.ktor.server.routing.RoutingContext
import io.ktor.server.util.getValue
import server.endpoints.EndpointBase
import server.error.Errors
import server.response.respondFailure
import server.response.respondSuccess

object SectorEndpoint : EndpointBase("/sector/{sectorId}") {
    override suspend fun RoutingContext.endpoint() {
        val sectorId = try {
            val sectorId: Int by call.parameters
            sectorId
        } catch (_: ParameterConversionException) {
            return respondFailure(Errors.InvalidData)
        }

        val sector = ServerDatabase.instance.query { Sector.findById(sectorId) }
            ?: return respondFailure(Errors.ObjectNotFound)

        respondSuccess(sector)
    }
}
