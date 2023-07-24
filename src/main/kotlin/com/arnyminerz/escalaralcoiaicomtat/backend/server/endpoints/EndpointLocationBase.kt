package com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints

import io.ktor.server.application.ApplicationCall
import io.ktor.util.pipeline.PipelineContext

abstract class EndpointLocationBase <T: Any> {
    suspend fun call(context: PipelineContext<Unit, ApplicationCall>, data: T) {
        with(context) { endpoint(data) }
    }

    protected abstract suspend fun PipelineContext<Unit, ApplicationCall>.endpoint(data: T)
}
