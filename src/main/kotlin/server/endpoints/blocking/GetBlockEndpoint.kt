package server.endpoints.blocking

import ServerDatabase
import database.entity.Blocking
import database.entity.Path
import database.table.BlockingTable
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.util.getValue
import io.ktor.util.pipeline.PipelineContext
import server.endpoints.EndpointBase
import server.error.Errors
import server.response.query.BlocksResponseData
import server.response.respondFailure
import server.response.respondSuccess

object GetBlockEndpoint: EndpointBase("/block/{pathId}") {
    override suspend fun PipelineContext<Unit, ApplicationCall>.endpoint() {
        val pathId: Int by call.parameters

        // Check that the path exists
        ServerDatabase.instance.query { Path.findById(pathId) }
            ?: return call.respondFailure(Errors.ObjectNotFound)

        val blocks = ServerDatabase.instance.query {
            Blocking.find { BlockingTable.path eq pathId }
                .toList()
        }

        respondSuccess(
            BlocksResponseData(blocks)
        )
    }
}
