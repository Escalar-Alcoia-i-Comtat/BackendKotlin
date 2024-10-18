package server.response

import database.serialization.Json
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.RoutingContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

@Serializable
data class SuccessResponse(
    val data: JsonElement? = null
): Response {
    override val success: Boolean = true

    inline fun <reified DataType: ResponseData> data(): DataType? = data?.let(Json::decodeFromJsonElement)
}

/**
 * Responds to the client with a success JSON response.
 *
 * @param httpStatusCode The HTTP status code to be sent in the response. The default value is [HttpStatusCode.OK].
 */
suspend inline fun RoutingContext.respondSuccess(
    httpStatusCode: HttpStatusCode = HttpStatusCode.OK
) {
    call.respond(
        status = httpStatusCode,
        message = SuccessResponse()
    )
}

/**
 * Responds to the client with a success JSON response.
 *
 * @param data The optional JSON object containing the data to be sent in the response.
 * @param httpStatusCode The HTTP status code to be sent in the response. The default value is [HttpStatusCode.OK].
 */
suspend inline fun <reified DataType: ResponseData> RoutingContext.respondSuccess(
    data: DataType? = null,
    httpStatusCode: HttpStatusCode = HttpStatusCode.OK
) {
    call.respond(
        status = httpStatusCode,
        message = SuccessResponse(Json.encodeToJsonElement(data))
    )
}
