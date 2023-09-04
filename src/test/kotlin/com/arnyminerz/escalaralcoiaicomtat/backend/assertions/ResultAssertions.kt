package com.arnyminerz.escalaralcoiaicomtat.backend.assertions

import com.arnyminerz.escalaralcoiaicomtat.backend.server.error.Error
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.getJSONArrayOrNull
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.getJSONObjectOrNull
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.json
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import org.json.JSONObject

/**
 * Asserts that the request was successful.
 *
 * @param statusCode If the request doesn't respond OK (200), you can modify it here.
 * @param block If any, allows handling the response if any.
 */
suspend fun HttpResponse.assertSuccess(
    statusCode: HttpStatusCode = HttpStatusCode.OK,
    block: ((data: JSONObject?) -> Unit)? = null
) {
    val json = bodyAsText().json

    assertEquals(
        statusCode,
        status,
        "expected: $statusCode but was: $status. StackTrace: ${json.getJSONObjectOrNull("error")?.getJSONArrayOrNull("stackTrace")?.joinToString("\n  ")}"
    )

    assertEquals(true, json.getBoolean("success"))
    block?.invoke(json.getJSONObjectOrNull("data"))
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
    assertEquals(
        error.status,
        status,
        "expected: ${error.status} but was: $status. StackTrace: ${json.getJSONObjectOrNull("error")?.getJSONArrayOrNull("stackTrace")?.joinToString("\n  ")}"
    )

    assertFalse(json.getBoolean("success"))

    val errorJson = json.getJSONObjectOrNull("error")
    assertNotNull(errorJson)
    assertEquals(error.code, errorJson.getInt("code"))
    assertEquals(error.message, errorJson.getString("message"))
}
