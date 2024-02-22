package server.endpoints.delete

import ServerDatabase
import database.entity.Sector
import database.entity.info.LastUpdate
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.util.getValue
import io.ktor.util.pipeline.PipelineContext
import server.endpoints.SecureEndpointBase
import server.error.Errors
import server.response.respondFailure
import server.response.respondSuccess

object DeleteSectorEndpoint : SecureEndpointBase("/sector/{sectorId}") {
    override suspend fun PipelineContext<Unit, ApplicationCall>.endpoint() {
        val sectorId: Int by call.parameters

        ServerDatabase.instance.query { Sector.findById(sectorId)?.delete() }
            ?: return respondFailure(Errors.ObjectNotFound)

        ServerDatabase.instance.query { LastUpdate.set() }

        respondSuccess()
    }
}
