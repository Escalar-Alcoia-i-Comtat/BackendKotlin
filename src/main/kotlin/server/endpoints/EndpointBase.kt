package server.endpoints

import diagnostics.Performance
import io.ktor.server.application.ApplicationCall
import io.ktor.util.pipeline.PipelineContext

abstract class EndpointBase(endpoint: String) : EndpointModel(endpoint) {
    override suspend fun call(context: PipelineContext<Unit, ApplicationCall>) =
        Performance.measure("EndpointBase", endpoint) {
            with(context) { endpoint() }
        }
}
