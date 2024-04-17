package assertions

import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import io.ktor.http.HttpStatusCode
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import org.json.JSONObject
import server.error.Error
import utils.getJSONArrayOrNull
import utils.getJSONObjectOrNull
import utils.getStringOrNull
import utils.json

/**
 * Asserts that the request was successful.
 *
 * @param statusCode If the request doesn't respond OK (200), you can modify it here.
 * @param block If any, allows handling the response if any.
 */
suspend inline fun HttpResponse.assertSuccess(
    statusCode: HttpStatusCode = HttpStatusCode.OK,
    block: (data: JSONObject?) -> Unit = {}
) {
    val json = bodyAsText().json
    val error = json.getJSONObjectOrNull("error")
    val errorMessage = error?.getStringOrNull("message")
    val errorType = error?.getStringOrNull("type")
    val stackTrace = error?.getJSONArrayOrNull("stackTrace")?.joinToString("\n    ")

    assertEquals(
        statusCode,
        status,
        StringBuilder().apply {
            appendLine("expected: $statusCode but was: $status")
            appendLine("Url: ${request.url}")
            if (errorMessage != null) {
                appendLine("Message: $errorMessage")
            }
            if (errorType != null) {
                appendLine("Type: $errorType")
            }
            if (stackTrace != null) {
                appendLine("Stack trace:\n    $stackTrace")
            }
        }.toString()
    )

    assertEquals(true, json.getBoolean("success"))
    block(json.getJSONObjectOrNull("data"))
}

/**
 * Asserts that the HttpResponse represents a failure based on the given error.
 *
 * @param error the expected error object.
 */
suspend fun HttpResponse.assertFailure(
    error: Error
) {
    val json = bodyAsText().json
    val responseError = json.getJSONObjectOrNull("error")
    val errorMessage = json.getStringOrNull("message")
    val errorType = responseError?.getStringOrNull("type")
    val stackTrace = responseError?.getJSONArrayOrNull("stackTrace")?.joinToString("\n    ")

    assertEquals(
        error.status,
        status,
        StringBuilder().apply {
            appendLine("expected: ${error.status} but was: $status")
            if (errorMessage != null) {
                appendLine("Message: $errorMessage")
            }
            if (errorType != null) {
                appendLine("Type: $errorType")
            }
            if (stackTrace != null) {
                appendLine("Stack trace:\n    $stackTrace")
            }
        }.toString()
    )

    assertFalse(json.getBoolean("success"))

    val errorJson = json.getJSONObjectOrNull("error")
    assertNotNull(errorJson)
    assertEquals(error.code, errorJson.getInt("code"))
    assertEquals(error.message, errorJson.getString("message"))
}
