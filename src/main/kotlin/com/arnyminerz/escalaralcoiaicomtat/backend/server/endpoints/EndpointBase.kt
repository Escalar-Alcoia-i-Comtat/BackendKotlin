package com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints

import io.ktor.server.application.ApplicationCall
import io.ktor.util.pipeline.PipelineContext

abstract class EndpointBase {
    suspend fun call(context: PipelineContext<Unit, ApplicationCall>) {
        with(context) { endpoint() }
    }

    protected abstract suspend fun PipelineContext<Unit, ApplicationCall>.endpoint()
}
