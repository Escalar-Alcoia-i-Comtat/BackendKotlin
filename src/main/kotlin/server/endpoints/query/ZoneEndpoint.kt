package server.endpoints.query

import ServerDatabase
import database.entity.Zone
import io.ktor.server.plugins.ParameterConversionException
import io.ktor.server.routing.RoutingContext
import io.ktor.server.util.getValue
import server.endpoints.EndpointBase
import server.error.Errors
import server.response.respondFailure
import server.response.respondSuccess

object ZoneEndpoint : EndpointBase("/zone/{zoneId}") {
    override suspend fun RoutingContext.endpoint() {
        val zoneId = try {
            val zoneId: Int by call.parameters
            zoneId
        } catch (_: ParameterConversionException) {
            return respondFailure(Errors.InvalidData)
        }

        val zone = ServerDatabase.instance.query { Zone.findById(zoneId) }
            ?: return respondFailure(Errors.ObjectNotFound)

        respondSuccess(zone)
    }
}
