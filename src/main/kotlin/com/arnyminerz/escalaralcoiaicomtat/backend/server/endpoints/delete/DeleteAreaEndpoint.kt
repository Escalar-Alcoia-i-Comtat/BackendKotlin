package com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.delete

import com.arnyminerz.escalaralcoiaicomtat.backend.ServerDatabase
import com.arnyminerz.escalaralcoiaicomtat.backend.database.entity.Area
import com.arnyminerz.escalaralcoiaicomtat.backend.database.entity.info.LastUpdate
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.SecureEndpointBase
import com.arnyminerz.escalaralcoiaicomtat.backend.server.error.Errors
import com.arnyminerz.escalaralcoiaicomtat.backend.server.response.respondFailure
import com.arnyminerz.escalaralcoiaicomtat.backend.server.response.respondSuccess
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.util.getValue
import io.ktor.util.pipeline.PipelineContext

object DeleteAreaEndpoint : SecureEndpointBase("/area/{areaId}") {
    override suspend fun PipelineContext<Unit, ApplicationCall>.endpoint() {
        val areaId: Int by call.parameters

        ServerDatabase.instance.query { Area.findById(areaId)?.delete() }
            ?: return respondFailure(Errors.ObjectNotFound)

        ServerDatabase.instance.query { LastUpdate.set() }

        respondSuccess()
    }
}
