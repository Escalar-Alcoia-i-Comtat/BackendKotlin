package com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.delete

import com.arnyminerz.escalaralcoiaicomtat.backend.ServerDatabase
import com.arnyminerz.escalaralcoiaicomtat.backend.database.entity.Blocking
import com.arnyminerz.escalaralcoiaicomtat.backend.database.entity.Path
import com.arnyminerz.escalaralcoiaicomtat.backend.database.table.BlockingTable
import com.arnyminerz.escalaralcoiaicomtat.backend.localization.Localization
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.SecureEndpointBase
import com.arnyminerz.escalaralcoiaicomtat.backend.server.error.Errors
import com.arnyminerz.escalaralcoiaicomtat.backend.server.response.respondFailure
import com.arnyminerz.escalaralcoiaicomtat.backend.server.response.respondSuccess
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.util.getValue
import io.ktor.util.pipeline.PipelineContext

object DeletePathEndpoint : SecureEndpointBase() {
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

        respondSuccess()
    }
}
