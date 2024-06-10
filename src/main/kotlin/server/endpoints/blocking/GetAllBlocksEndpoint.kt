package server.endpoints.blocking

import ServerDatabase
import database.entity.Blocking
import io.ktor.server.application.ApplicationCall
import io.ktor.util.pipeline.PipelineContext
import server.endpoints.EndpointBase
import server.response.query.BlocksResponseData
import server.response.respondSuccess

object GetAllBlocksEndpoint: EndpointBase("/blocks") {
    override suspend fun PipelineContext<Unit, ApplicationCall>.endpoint() {
        // Check that the path exists
        val blocks = ServerDatabase.instance.query { Blocking.all().toList() }

        respondSuccess(
            data = BlocksResponseData(blocks)
        )
    }
}
