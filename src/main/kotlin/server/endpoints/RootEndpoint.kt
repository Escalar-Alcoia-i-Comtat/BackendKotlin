package server.endpoints

import io.ktor.server.routing.RoutingContext
import server.response.respondSuccess

object RootEndpoint: EndpointBase("/") {
    override suspend fun RoutingContext.endpoint() {
        respondSuccess()
    }
}
