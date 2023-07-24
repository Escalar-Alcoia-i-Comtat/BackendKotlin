package com.arnyminerz.escalaralcoiaicomtat.backend.server.error

import com.arnyminerz.escalaralcoiaicomtat.backend.utils.jsonOf
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.serialization.JsonSerializable
import io.ktor.http.HttpStatusCode
import org.json.JSONObject

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
    override fun toJson(): JSONObject = jsonOf("code" to code, "message" to message)
}
