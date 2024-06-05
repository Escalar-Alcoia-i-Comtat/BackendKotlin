package server.endpoints.legacy.old

import KoverIgnore
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.util.getValue
import io.ktor.util.pipeline.PipelineContext
import migration.OldDataMigrationSingleton
import server.endpoints.EndpointBase
import server.error.Errors
import server.response.respondFailure
import server.response.respondSuccess

/**
 * Starts an asynchronous task to import some data into the database. Progress can then be supervised.
 */
@KoverIgnore
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
