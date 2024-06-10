package server.error

import io.ktor.http.HttpStatusCode
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * Represents an error response.
 *
 * @property code The error code.
 * @property message The error message.
 * @property status The HTTP status code associated with the error (default: HttpStatusCode.BadRequest).
 */
@Serializable
data class Error(
    val code: Int,
    val message: String,
    val status: Int = HttpStatusCode.BadRequest.value,
    val extra: String? = null,
    val type: String? = null,
    val stackTrace: List<String>? = null,
    val errors: List<ErrorMeta>? = null
) {
    constructor(code: Int, message: String, status: HttpStatusCode) : this(code, message, status.value)

    @Transient
    val statusCode: HttpStatusCode = HttpStatusCode.fromValue(status)

    @Serializable
    data class ErrorMeta(
        val key: String,
        val errors: List<Value>
    ) {
        @Serializable
        data class Value(
            val code: String,
            val message: String
        )
    }
}
