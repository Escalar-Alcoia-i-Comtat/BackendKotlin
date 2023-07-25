package com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.query

import com.arnyminerz.escalaralcoiaicomtat.backend.ServerDatabase
import com.arnyminerz.escalaralcoiaicomtat.backend.database.entity.Sector
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.EndpointBase
import com.arnyminerz.escalaralcoiaicomtat.backend.server.error.Errors
import com.arnyminerz.escalaralcoiaicomtat.backend.server.response.respondFailure
import com.arnyminerz.escalaralcoiaicomtat.backend.server.response.respondSuccess
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.util.getValue
import io.ktor.util.pipeline.PipelineContext

object SectorEndpoint : EndpointBase() {
    override suspend fun PipelineContext<Unit, ApplicationCall>.endpoint() {
        val sectorId: Int by call.parameters

        val sector = ServerDatabase.instance.query { Sector.findById(sectorId) }
            ?: return respondFailure(Errors.ObjectNotFound)
        val sectorJson = ServerDatabase.instance.query { sector.toJson() }

        respondSuccess(sectorJson)
    }
}
