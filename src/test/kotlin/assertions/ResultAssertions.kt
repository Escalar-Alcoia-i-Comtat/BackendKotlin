package assertions

import ServerDatabase
import database.serialization.Json
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import io.ktor.http.HttpStatusCode
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.serialization.SerializationException
import server.error.Error
import server.response.FailureResponse
import server.response.Response
import server.response.ResponseData
import server.response.SuccessResponse

/**
 * Asserts that the request was successful.
 *
 * @param statusCode If the request doesn't respond OK (200), you can modify it here.
 */
suspend fun HttpResponse.assertSuccess(
    statusCode: HttpStatusCode = HttpStatusCode.OK
) {
    val response = body<Response>()

    if (response.success) {
        assertEquals(statusCode, status)
        assertTrue(response.success)
    } else {
        var errorMessage: String? = null
        var errorType: String? = null
        var stackTrace: String? = null
        if (response is FailureResponse) {
            errorMessage = response.error?.message
            errorType = response.error?.type
            stackTrace = response.error?.stackTrace?.joinToString("\n    ")
        }

        throw AssertionError(
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
    }
}

fun interface SuccessfulAssertionBlock<Type : ResponseData> {
    suspend operator fun invoke(data: Type?)
}

/**
 * Asserts that the request was successful.
 *
 * @param statusCode If the request doesn't respond OK (200), you can modify it here.
 * @param block If any, allows handling the response if any.
 */
suspend inline fun HttpResponse.assertSuccessRaw(
    statusCode: HttpStatusCode = HttpStatusCode.OK,
    block: (body: String) -> Unit = {}
) {
    val response = body<Response>()

    var errorMessage: String? = null
    var errorType: String? = null
    var stackTrace: String? = null
    if (response is FailureResponse) {
        errorMessage = response.error?.message
        errorType = response.error?.type
        stackTrace = response.error?.stackTrace?.joinToString("\n    ")
    }

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

    assertTrue(response.success)
    assertIs<SuccessResponse>(response)
    block(bodyAsText())
}

/**
 * Asserts that the request was successful.
 *
 * @param statusCode If the request doesn't respond OK (200), you can modify it here.
 * @param block If any, allows handling the response if any.
 */
suspend inline fun <reified Type : ResponseData> HttpResponse.assertSuccess(
    statusCode: HttpStatusCode = HttpStatusCode.OK,
    block: SuccessfulAssertionBlock<Type> = SuccessfulAssertionBlock {}
) = assertSuccessRaw(statusCode) {
    try {
        val data: SuccessResponse = Json.decodeFromString<SuccessResponse>(it)
        ServerDatabase { block(data.data()) }
    } catch (e: SerializationException) {
        throw SerializationException("Failed to parse response:\n\t$it", e)
    }
}

/**
 * Asserts that the HttpResponse represents a failure based on the given error.
 *
 * @param error the expected error object.
 */
suspend fun HttpResponse.assertFailure(
    error: Error
) {
    val body = bodyAsText()

    val response = Json.decodeFromString<Response>(body)

    var errorMessage: String? = null
    var errorType: String? = null
    var stackTrace: String? = null
    if (response is FailureResponse) {
        errorMessage = response.error?.message
        errorType = response.error?.type
        stackTrace = response.error?.stackTrace?.joinToString("\n    ")
    }

    assertEquals(
        error.status,
        status.value,
        StringBuilder().apply {
            appendLine("expected: ${error.status} but was: ${status.value}")
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

    assertFalse(response.success)

    assertIs<FailureResponse>(response)
    assertEquals(error.code, response.error?.code)
    assertEquals(error.message, response.error?.message)
}
