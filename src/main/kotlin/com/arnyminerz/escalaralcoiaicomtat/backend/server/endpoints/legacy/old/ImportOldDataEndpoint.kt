package com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.legacy.old

import com.arnyminerz.escalaralcoiaicomtat.backend.migration.OldDataMigrationSingleton
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.EndpointBase
import com.arnyminerz.escalaralcoiaicomtat.backend.server.error.Errors
import com.arnyminerz.escalaralcoiaicomtat.backend.server.response.respondFailure
import com.arnyminerz.escalaralcoiaicomtat.backend.server.response.respondSuccess
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.util.getValue
import io.ktor.util.pipeline.PipelineContext

/**
 * Starts an asynchronous task to import some data into the database. Progress can then be supervised.
 */
object ImportOldDataEndpoint : EndpointBase("/import") {

    override suspend fun PipelineContext<Unit, ApplicationCall>.endpoint() {
        val hostname: String? by call.parameters

        if (hostname == null) {
            respondFailure(Errors.MissingData)
            return
        }

        val success = OldDataMigrationSingleton.run(hostname!!)
        if (success) {
            respondSuccess()
        } else {
            respondFailure(Errors.DatabaseNotEmpty)
        }
    }
}
