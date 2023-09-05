package com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.blocking

import com.arnyminerz.escalaralcoiaicomtat.backend.ServerDatabase
import com.arnyminerz.escalaralcoiaicomtat.backend.database.entity.Blocking
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.EndpointBase
import com.arnyminerz.escalaralcoiaicomtat.backend.server.response.respondSuccess
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.jsonOf
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.toJson
import io.ktor.server.application.ApplicationCall
import io.ktor.util.pipeline.PipelineContext

object GetAllBlocksEndpoint: EndpointBase() {
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
