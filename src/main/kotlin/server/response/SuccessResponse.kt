package server.response

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.util.pipeline.PipelineContext
import kotlinx.serialization.Serializable

@Serializable
data class SuccessResponse<DataType: ResponseData>(
    val data: DataType? = null
): Response {
    override val success: Boolean = true
}

/**
 * Responds to the client with a success JSON response.
 *
 * @param httpStatusCode The HTTP status code to be sent in the response. The default value is [HttpStatusCode.OK].
 */
suspend fun PipelineContext<Unit, ApplicationCall>.respondSuccess(
    httpStatusCode: HttpStatusCode = HttpStatusCode.OK
) {
    call.respond(
        status = httpStatusCode,
        message = SuccessResponse<ResponseData>()
    )
}

/**
 * Responds to the client with a success JSON response.
 *
 * @param data The optional JSON object containing the data to be sent in the response.
 * @param httpStatusCode The HTTP status code to be sent in the response. The default value is [HttpStatusCode.OK].
 */
suspend fun <DataType: ResponseData> PipelineContext<Unit, ApplicationCall>.respondSuccess(
    data: DataType? = null,
    httpStatusCode: HttpStatusCode = HttpStatusCode.OK
) {
    call.respond(
        status = httpStatusCode,
        message = SuccessResponse(data)
    )
}
