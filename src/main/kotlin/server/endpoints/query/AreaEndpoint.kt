package server.endpoints.query

import ServerDatabase
import database.entity.Area
import io.ktor.http.HttpHeaders
import io.ktor.server.plugins.ParameterConversionException
import io.ktor.server.response.header
import io.ktor.server.routing.RoutingContext
import io.ktor.server.util.getValue
import server.endpoints.EndpointBase
import server.error.Errors
import server.response.ResourceId
import server.response.ResourceType
import server.response.respondFailure
import server.response.respondSuccess

object AreaEndpoint : EndpointBase("/area/{areaId}") {
    override suspend fun RoutingContext.endpoint() {
        val areaId = try {
            val areaId: Int by call.parameters
            areaId
        } catch (_: ParameterConversionException) {
            return respondFailure(Errors.InvalidData)
        }

        val area = ServerDatabase.instance.query { Area.findById(areaId) }
            ?: return respondFailure(Errors.ObjectNotFound)

        call.response.header(HttpHeaders.ResourceType, "Area")
        call.response.header(HttpHeaders.ResourceId, areaId.toString())

        respondSuccess(data = area)
    }
}
