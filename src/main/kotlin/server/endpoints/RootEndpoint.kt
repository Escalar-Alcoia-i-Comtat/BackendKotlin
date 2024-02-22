package server.endpoints

import io.ktor.server.application.ApplicationCall
import io.ktor.util.pipeline.PipelineContext
import server.response.respondSuccess

object RootEndpoint: EndpointBase("/") {
    override suspend fun PipelineContext<Unit, ApplicationCall>.endpoint() {
        respondSuccess()
    }
}
