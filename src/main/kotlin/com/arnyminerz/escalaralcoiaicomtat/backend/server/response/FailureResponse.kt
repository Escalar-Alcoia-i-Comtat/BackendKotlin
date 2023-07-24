package com.arnyminerz.escalaralcoiaicomtat.backend.server.response

import com.arnyminerz.escalaralcoiaicomtat.backend.server.error.Error
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.jsonOf
import io.ktor.http.ContentType
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.response.respondText
import io.ktor.util.pipeline.PipelineContext
import org.json.JSONObject

/**
 * Responds with a failure message to the client.
 *
 * @param error The error object containing the error code and message.
 */
suspend fun PipelineContext<Unit, ApplicationCall>.respondFailure(
    error: Error
) {
    call.respondText(
        JSONObject().apply {
            put("success", false)
            put("error", jsonOf("code" to error.code, "message" to error.message))
        }.toString(),
        contentType = ContentType.Application.Json,
        status = error.status
    )
}
