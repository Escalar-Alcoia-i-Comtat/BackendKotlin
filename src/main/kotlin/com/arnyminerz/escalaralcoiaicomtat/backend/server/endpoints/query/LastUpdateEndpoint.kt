package com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.query

import com.arnyminerz.escalaralcoiaicomtat.backend.ServerDatabase
import com.arnyminerz.escalaralcoiaicomtat.backend.database.entity.info.LastUpdate
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.EndpointBase
import com.arnyminerz.escalaralcoiaicomtat.backend.server.response.respondSuccess
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.jsonOf
import io.ktor.server.application.ApplicationCall
import io.ktor.util.pipeline.PipelineContext

object LastUpdateEndpoint: EndpointBase("/last_update") {
    override suspend fun PipelineContext<Unit, ApplicationCall>.endpoint() {
        val lastUpdate = ServerDatabase.instance.query { LastUpdate.get() }

        respondSuccess(
            data = jsonOf("last_update" to lastUpdate)
        )
    }
}
