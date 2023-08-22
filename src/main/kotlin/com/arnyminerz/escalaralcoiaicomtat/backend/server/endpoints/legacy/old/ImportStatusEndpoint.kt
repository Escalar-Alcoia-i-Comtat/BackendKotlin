package com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.legacy.old

import com.arnyminerz.escalaralcoiaicomtat.backend.Logger
import com.arnyminerz.escalaralcoiaicomtat.backend.migration.OldDataMigrationSingleton
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.EndpointBase
import com.arnyminerz.escalaralcoiaicomtat.backend.server.response.respondSuccess
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.jsonOf
import io.ktor.server.application.ApplicationCall
import io.ktor.util.pipeline.PipelineContext
import org.json.JSONArray

object ImportStatusEndpoint : EndpointBase() {

    override suspend fun PipelineContext<Unit, ApplicationCall>.endpoint() {
        val singleton = OldDataMigrationSingleton.getInstance()

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
