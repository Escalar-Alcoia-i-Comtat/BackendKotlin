package server.endpoints.delete

import ServerDatabase
import database.EntityTypes
import database.entity.Area
import database.entity.info.LastUpdate
import distribution.Notifier
import io.ktor.server.routing.RoutingContext
import io.ktor.server.util.getValue
import server.endpoints.SecureEndpointBase
import server.error.Errors
import server.response.respondFailure
import server.response.respondSuccess

object DeleteAreaEndpoint : SecureEndpointBase("/area/{areaId}") {
    override suspend fun RoutingContext.endpoint() {
        val areaId: Int by call.parameters

        val area = ServerDatabase.instance.query { Area.findById(areaId)?.also(Area::delete) }
            ?: return respondFailure(Errors.ObjectNotFound)

        // Delete the image file
        area.image.delete()

        ServerDatabase.instance.query { LastUpdate.set() }

        Notifier.getInstance().notifyDeleted(EntityTypes.AREA, areaId)

        respondSuccess()
    }
}
