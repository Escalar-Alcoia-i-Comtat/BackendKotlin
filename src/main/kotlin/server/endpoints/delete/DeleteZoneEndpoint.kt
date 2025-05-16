package server.endpoints.delete

import ServerDatabase
import database.EntityTypes
import database.entity.Zone
import database.entity.info.LastUpdate
import distribution.Notifier
import io.ktor.server.routing.RoutingContext
import io.ktor.server.util.getValue
import server.endpoints.SecureEndpointBase
import server.error.Errors
import server.response.respondFailure
import server.response.respondSuccess

object DeleteZoneEndpoint : SecureEndpointBase("/zone/{zoneId}") {
    override suspend fun RoutingContext.endpoint() {
        val zoneId: Int by call.parameters

        val zone = ServerDatabase.instance.query { Zone.findById(zoneId)?.also(Zone::deleteRecursively) }
            ?: return respondFailure(Errors.ObjectNotFound)

        // Delete the image and KMX files
        zone.image.delete()
        zone.kmz.delete()

        ServerDatabase.instance.query { LastUpdate.set() }

        Notifier.getInstance().notifyDeleted(EntityTypes.ZONE, zoneId)

        respondSuccess()
    }
}
