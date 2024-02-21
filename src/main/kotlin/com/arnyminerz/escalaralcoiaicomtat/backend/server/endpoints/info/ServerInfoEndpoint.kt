package com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.info

import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.EndpointBase
import com.arnyminerz.escalaralcoiaicomtat.backend.server.response.respondSuccess
import com.arnyminerz.escalaralcoiaicomtat.backend.system.Package
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.jsonOf
import io.ktor.server.application.ApplicationCall
import io.ktor.util.pipeline.PipelineContext

object ServerInfoEndpoint: EndpointBase("/info") {
    override suspend fun PipelineContext<Unit, ApplicationCall>.endpoint() {
        val version = Package.getVersion()

        respondSuccess(
            data = jsonOf("version" to version)
        )
    }
}
