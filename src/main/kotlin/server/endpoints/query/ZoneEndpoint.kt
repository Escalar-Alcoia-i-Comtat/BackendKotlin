package server.endpoints.query

import ServerDatabase
import database.entity.Zone
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.plugins.ParameterConversionException
import io.ktor.server.util.getValue
import io.ktor.util.pipeline.PipelineContext
import server.endpoints.EndpointBase
import server.error.Errors
import server.response.respondFailure
import server.response.respondSuccess

object ZoneEndpoint : EndpointBase("/zone/{zoneId}") {
    override suspend fun PipelineContext<Unit, ApplicationCall>.endpoint() {
        val zoneId = try {
            val zoneId: Int by call.parameters
            zoneId
        } catch (_: ParameterConversionException) {
            return respondFailure(Errors.InvalidData)
        }

        val zone = ServerDatabase.instance.query { Zone.findById(zoneId) }
            ?: return respondFailure(Errors.ObjectNotFound)
        val zoneJson = ServerDatabase.instance.query { zone.toJson() }

        respondSuccess(zoneJson)
    }
}
