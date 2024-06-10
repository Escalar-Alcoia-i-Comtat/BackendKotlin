package server.endpoints

import diagnostics.Performance
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.ContentTransformationException
import io.ktor.util.pipeline.PipelineContext
import server.error.Errors
import server.response.respondFailure

abstract class EndpointBase(endpoint: String) : EndpointModel(endpoint) {
    override suspend fun call(context: PipelineContext<Unit, ApplicationCall>) =
        Performance.measure("EndpointBase", endpoint) {
            try {
                with(context) { endpoint() }
            }catch (_: ContentTransformationException) {
                context.respondFailure(Errors.MissingData)
            }
        }
}
