package server.endpoints.delete

import ServerDatabase
import database.EntityTypes
import database.entity.Sector
import database.entity.info.LastUpdate
import distribution.Notifier
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

        val sector = ServerDatabase.instance.query { Sector.findById(sectorId)?.also(Sector::delete) }
            ?: return respondFailure(Errors.ObjectNotFound)

        // Delete the image file and GPX if exists
        sector.image.delete()
        sector.gpx?.delete()

        ServerDatabase.instance.query { LastUpdate.set() }

        Notifier.getInstance().notifyDeleted(EntityTypes.SECTOR, sectorId)

        respondSuccess()
    }
}
