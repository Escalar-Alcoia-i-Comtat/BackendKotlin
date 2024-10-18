package server.endpoints.delete

import ServerDatabase
import database.EntityTypes
import database.entity.Blocking
import database.entity.Path
import database.entity.info.LastUpdate
import database.table.BlockingTable
import distribution.Notifier
import io.ktor.server.routing.RoutingContext
import io.ktor.server.util.getValue
import java.io.File
import localization.Localization
import server.endpoints.SecureEndpointBase
import server.error.Errors
import server.response.respondFailure
import server.response.respondSuccess

object DeletePathEndpoint : SecureEndpointBase("/path/{pathId}") {
    override suspend fun RoutingContext.endpoint() {
        val pathId: Int by call.parameters

        val path = ServerDatabase.instance.query { Path.findById(pathId) }
            ?: return respondFailure(Errors.ObjectNotFound)

        // All blocks must be deleted before removing the path
        ServerDatabase.instance.query {
            val blocks = Blocking.find { BlockingTable.path eq path.id }
            blocks.forEach {
                it.delete()
                Notifier.getInstance().notifyDeleted(EntityTypes.BLOCKING, it.id.value)
            }
        }

        // Delete the path's description from Crowdin if any
        Localization.deletePathDescription(path)

        // Delete the path's images if any
        path.images?.forEach(File::delete)

        // Now remove the path
        ServerDatabase.instance.query { path.delete() }

        ServerDatabase.instance.query { with(LastUpdate) { set() } }

        Notifier.getInstance().notifyDeleted(EntityTypes.PATH, pathId)

        respondSuccess()
    }
}
