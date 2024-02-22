package server.endpoints.query

import ServerDatabase
import database.entity.info.LastUpdate
import io.ktor.server.application.ApplicationCall
import io.ktor.util.pipeline.PipelineContext
import server.endpoints.EndpointBase
import server.response.respondSuccess
import utils.jsonOf

object LastUpdateEndpoint: EndpointBase("/last_update") {
    override suspend fun PipelineContext<Unit, ApplicationCall>.endpoint() {
        val lastUpdate = ServerDatabase.instance.query { LastUpdate.get() }

        respondSuccess(
            data = jsonOf("last_update" to lastUpdate)
        )
    }
}
