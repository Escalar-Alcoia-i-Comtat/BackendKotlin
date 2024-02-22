package server.endpoints.blocking

import ServerDatabase
import database.entity.Blocking
import io.ktor.server.application.ApplicationCall
import io.ktor.util.pipeline.PipelineContext
import server.endpoints.EndpointBase
import server.response.respondSuccess
import utils.jsonOf
import utils.toJson

object GetAllBlocksEndpoint: EndpointBase("/blocks") {
    override suspend fun PipelineContext<Unit, ApplicationCall>.endpoint() {
        // Check that the path exists
        val blocks = ServerDatabase.instance.query {
            Blocking.all().toJson()
        }

        respondSuccess(
            jsonOf(
                "blocks" to blocks
            )
        )
    }
}
