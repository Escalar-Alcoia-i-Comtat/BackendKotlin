package server.endpoints.delete

import ServerDatabase
import database.entity.Blocking
import database.entity.Path
import database.entity.info.LastUpdate
import database.table.BlockingTable
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.util.getValue
import io.ktor.util.pipeline.PipelineContext
import localization.Localization
import server.endpoints.SecureEndpointBase
import server.error.Errors
import server.response.respondFailure
import server.response.respondSuccess

object DeletePathEndpoint : SecureEndpointBase("/path/{pathId}") {
    override suspend fun PipelineContext<Unit, ApplicationCall>.endpoint() {
        val pathId: Int by call.parameters

        val path = ServerDatabase.instance.query { Path.findById(pathId) }
            ?: return respondFailure(Errors.ObjectNotFound)

        // All blocks must be deleted before removing the path
        ServerDatabase.instance.query {
            val blocks = Blocking.find { BlockingTable.path eq path.id }
            blocks.forEach { it.delete() }
        }

        // Delete the path's description from Crowdin if any
        Localization.deletePathDescription(path)

        // Now remove the path
        ServerDatabase.instance.query { path.delete() }

        ServerDatabase.instance.query { LastUpdate.set() }

        respondSuccess()
    }
}
