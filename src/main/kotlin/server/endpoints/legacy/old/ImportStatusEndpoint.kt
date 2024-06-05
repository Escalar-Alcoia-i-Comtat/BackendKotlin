package server.endpoints.legacy.old

import KoverIgnore
import Logger
import io.ktor.server.application.ApplicationCall
import io.ktor.util.pipeline.PipelineContext
import migration.OldDataMigrationSingleton
import org.json.JSONArray
import server.endpoints.EndpointBase
import server.error.Errors
import server.response.respondFailure
import server.response.respondSuccess
import utils.jsonOf

@KoverIgnore
object ImportStatusEndpoint : EndpointBase("/import/status") {

    override suspend fun PipelineContext<Unit, ApplicationCall>.endpoint() {
        val singleton = OldDataMigrationSingleton.getInstance()

        if (singleton == null) {
            respondFailure(Errors.NotRunning)
        } else {
            respondSuccess(
                jsonOf(
                    "is_running" to singleton.isRunning,
                    "is_finished" to singleton.isFinished,
                    "error" to singleton.error?.let { throwable ->
                        jsonOf(
                            "message" to throwable.message,
                            "type" to throwable::class.java.simpleName,
                            "stackTrace" to JSONArray().apply {
                                putAll(throwable.stackTrace)
                            }
                        )
                    },
                    "step" to singleton.step,
                    "progress" to singleton.progress,
                    "max" to singleton.max,
                    "logs" to Logger.trace
                )
            )
        }
    }
}
