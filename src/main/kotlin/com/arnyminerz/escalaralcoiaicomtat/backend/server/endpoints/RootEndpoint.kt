package com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints

import com.arnyminerz.escalaralcoiaicomtat.backend.server.response.respondSuccess
import io.ktor.server.application.ApplicationCall
import io.ktor.util.pipeline.PipelineContext

object RootEndpoint: EndpointBase() {
    override suspend fun PipelineContext<Unit, ApplicationCall>.endpoint() {
        respondSuccess()
    }
}
