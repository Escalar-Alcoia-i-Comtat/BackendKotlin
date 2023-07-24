package com.arnyminerz.escalaralcoiaicomtat.backend.server.response

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.response.respondText
import io.ktor.util.pipeline.PipelineContext
import org.json.JSONObject

/**
 * Responds to the client with a success JSON response.
 *
 * @param data The optional JSON object containing the data to be sent in the response.
 * @param httpStatusCode The HTTP status code to be sent in the response. The default value is [HttpStatusCode.OK].
 */
suspend fun PipelineContext<Unit, ApplicationCall>.respondSuccess(
    data: JSONObject? = null,
    httpStatusCode: HttpStatusCode = HttpStatusCode.OK
) {
    call.respondText(
        JSONObject().apply {
            put("success", true)
            put("data", data)
        }.toString(),
        ContentType.Application.Json,
        httpStatusCode
    )
}
