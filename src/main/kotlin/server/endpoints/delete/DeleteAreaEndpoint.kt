package server.endpoints.delete

import ServerDatabase
import database.EntityTypes
import database.entity.Area
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

object DeleteAreaEndpoint : SecureEndpointBase("/area/{areaId}") {
    override suspend fun PipelineContext<Unit, ApplicationCall>.endpoint() {
        val areaId: Int by call.parameters

        val area = ServerDatabase.instance.query { Area.findById(areaId)?.also(Area::delete) }
            ?: return respondFailure(Errors.ObjectNotFound)

        // Delete the image file
        area.image.delete()

        ServerDatabase.instance.query { LastUpdate.set() }

        DeviceNotifier.notifyDeleted(EntityTypes.AREA, areaId)

        respondSuccess()
    }
}
