package server.endpoints.info

import io.ktor.server.application.ApplicationCall
import io.ktor.util.pipeline.PipelineContext
import server.endpoints.EndpointBase
import server.response.respondSuccess
import system.Package
import utils.jsonOf

object ServerInfoEndpoint: EndpointBase("/info") {
    override suspend fun PipelineContext<Unit, ApplicationCall>.endpoint() {
        val version = Package.getVersion()

        respondSuccess(
            data = jsonOf("version" to version)
        )
    }
}
