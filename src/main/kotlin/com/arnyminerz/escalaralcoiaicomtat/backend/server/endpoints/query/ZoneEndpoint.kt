package com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.query

import com.arnyminerz.escalaralcoiaicomtat.backend.ServerDatabase
import com.arnyminerz.escalaralcoiaicomtat.backend.database.entity.Zone
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.EndpointBase
import com.arnyminerz.escalaralcoiaicomtat.backend.server.error.Errors
import com.arnyminerz.escalaralcoiaicomtat.backend.server.response.respondFailure
import com.arnyminerz.escalaralcoiaicomtat.backend.server.response.respondSuccess
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.util.getValue
import io.ktor.util.pipeline.PipelineContext

object ZoneEndpoint : EndpointBase() {
    override suspend fun PipelineContext<Unit, ApplicationCall>.endpoint() {
        val zoneId: Int by call.parameters

        val zone = ServerDatabase.instance.query { Zone.findById(zoneId) } ?: return respondFailure(Errors.ObjectNotFound)
        val zoneJson = ServerDatabase.instance.query { zone.toJson() }

        respondSuccess(zoneJson)
    }
}
