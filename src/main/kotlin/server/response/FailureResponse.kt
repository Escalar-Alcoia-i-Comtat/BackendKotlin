package server.response

import com.crowdin.client.core.http.exceptions.HttpBadRequestException
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.util.pipeline.PipelineContext
import kotlinx.serialization.Serializable
import server.error.Error

@Serializable
data class FailureResponse(
    val error: Error? = null
): Response {
    override val success: Boolean = false
}

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
    println("Responding with status: ${error.status}")
    response.status(error.statusCode)
    respond(FailureResponse(error.copy(extra = extra)))
}

/**
 * Responds with a failure message to the client.
 *
 * @param throwable The exception thrown that shall be logged.
 */
suspend fun ApplicationCall.respondFailure(throwable: Throwable) {
    respondFailure(
        Error(
            code = -1,
            message = throwable.message ?: "An error occurred",
            status = HttpStatusCode.InternalServerError
        ).copy(
            type = throwable::class.java.simpleName,
            stackTrace = throwable.stackTrace.map { it.toString() },
            errors = if (throwable is HttpBadRequestException) {
                throwable.errors.map { errorHolder ->
                    val holdingError = errorHolder.error
                    Error.ErrorMeta(
                        key = holdingError.key,
                        errors = holdingError.errors.map { error ->
                            Error.ErrorMeta.Value(
                                code = error.code,
                                message = error.message
                            )
                        }
                    )
                }
            } else null
        )
    )
}
