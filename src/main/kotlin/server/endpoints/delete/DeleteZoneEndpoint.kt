package server.endpoints.delete

import ServerDatabase
import database.EntityTypes
import database.entity.Zone
import database.entity.info.LastUpdate
import distribution.DeviceNotifier
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.util.getValue
import io.ktor.util.pipeline.PipelineContext
import server.endpoints.SecureEndpointBase
import server.error.Errors
import server.response.respondFailure
import server.response.respondSuccess

object DeleteZoneEndpoint : SecureEndpointBase("/zone/{zoneId}") {
    override suspend fun PipelineContext<Unit, ApplicationCall>.endpoint() {
        val zoneId: Int by call.parameters

        val zone = ServerDatabase.instance.query { Zone.findById(zoneId)?.also(Zone::delete) }
            ?: return respondFailure(Errors.ObjectNotFound)

        // Delete the image and KMX files
        zone.image.delete()
        zone.kmz.delete()

        ServerDatabase.instance.query { LastUpdate.set() }

        DeviceNotifier.notifyDeleted(EntityTypes.ZONE, zoneId)

        respondSuccess()
    }
}
