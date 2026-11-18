package server.endpoints

import io.ktor.server.plugins.BadRequestException
import io.ktor.server.routing.RoutingContext
import server.error.Errors
import server.response.respondFailure

abstract class EndpointBase(endpoint: String) : EndpointModel(endpoint) {
    override suspend fun call(context: RoutingContext) = try {
        with(context) { endpoint() }
    } catch (_: BadRequestException) {
        context.respondFailure(Errors.MissingData)
    }
}
