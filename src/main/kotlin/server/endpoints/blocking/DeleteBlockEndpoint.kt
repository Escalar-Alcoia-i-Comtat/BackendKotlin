package server.endpoints.blocking

import ServerDatabase
import database.entity.Blocking
import database.entity.info.LastUpdate
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.util.getValue
import io.ktor.util.pipeline.PipelineContext
import server.endpoints.SecureEndpointBase
import server.error.Errors
import server.response.respondFailure
import server.response.respondSuccess

object DeleteBlockEndpoint: SecureEndpointBase("/block/{blockId}") {
    override suspend fun PipelineContext<Unit, ApplicationCall>.endpoint() {
        val blockId: Int by call.parameters

        val block = ServerDatabase.instance.query {
            Blocking.findById(blockId)
        } ?: return respondFailure(Errors.ObjectNotFound)

        ServerDatabase.instance.query {
            block.delete()
        }

        ServerDatabase.instance.query { LastUpdate.set() }

        respondSuccess()
    }
}
