package server.response

import com.crowdin.client.core.http.exceptions.HttpBadRequestException
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.response.respondText
import io.ktor.util.pipeline.PipelineContext
import org.json.JSONArray
import org.json.JSONObject
import server.error.Error
import utils.jsonOf

/**
 * Responds with a failure message to the client.
 *
 * @param error The error object containing the error code and message.
 */
suspend fun PipelineContext<Unit, ApplicationCall>.respondFailure(
    error: Error, extra: String? = null
) {
    call.respondFailure(error, extra)
}

/**
 * Responds with a failure message to the client.
 *
 * @param error The error object containing the error code and message.
 */
suspend fun ApplicationCall.respondFailure(error: Error, extra: String? = null) {
    respondText(
        JSONObject().apply {
            put("success", false)
            put("error", jsonOf("code" to error.code, "message" to error.message, "extra" to extra))
        }.toString(),
        contentType = ContentType.Application.Json,
        status = error.status
    )
}

/**
 * Responds with a failure message to the client.
 *
 * @param throwable The exception thrown that shall be logged.
 */
suspend fun ApplicationCall.respondFailure(throwable: Throwable) {
    respondText(
        JSONObject().apply {
            put("success", false)
            put(
                "error",
                jsonOf(
                    "code" to -1,
                    "message" to throwable.message,
                    "type" to throwable::class.java.simpleName,
                    "stackTrace" to JSONArray().apply {
                        putAll(throwable.stackTrace)
                    },
                    "errors" to (throwable as? HttpBadRequestException)?.let { exception ->
                        JSONArray().apply {
                            putAll(
                                exception.errors.map { errorHolder ->
                                    val holdingError = errorHolder.error
                                    jsonOf(
                                        "key" to holdingError.key,
                                        "errors" to JSONArray().apply {
                                            holdingError.errors.forEach { error ->
                                                put(
                                                    jsonOf("code" to error.code, "message" to error.message)
                                                )
                                            }
                                        }
                                    )
                                }
                            )
                        }
                    }
                )
            )
        }.toString(),
        contentType = ContentType.Application.Json,
        status = HttpStatusCode.InternalServerError
    )
}
