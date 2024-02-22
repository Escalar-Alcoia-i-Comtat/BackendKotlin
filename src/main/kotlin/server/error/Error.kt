package server.error

import io.ktor.http.HttpStatusCode
import org.json.JSONObject
import system.EnvironmentVariables
import utils.jsonOf
import utils.serialization.JsonSerializable

/**
 * Represents an error response.
 *
 * @property code The error code.
 * @property message The error message.
 * @property status The HTTP status code associated with the error (default: HttpStatusCode.BadRequest).
 */
data class Error(
    val code: Int,
    val message: String,
    val status: HttpStatusCode = HttpStatusCode.BadRequest
): JsonSerializable {
    private var extra: JSONObject? = null

    fun withExtra(extra: JSONObject): Error {
        this.extra = extra
        return this
    }

    override fun toJson(): JSONObject = jsonOf(
        "code" to code,
        "message" to message,
        "extra" to extra?.takeIf { EnvironmentVariables.Environment.IsProduction.value != "true" }
    )
}
