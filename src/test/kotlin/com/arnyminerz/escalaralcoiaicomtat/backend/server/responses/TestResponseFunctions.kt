package com.arnyminerz.escalaralcoiaicomtat.backend.server.responses

import com.arnyminerz.escalaralcoiaicomtat.backend.assertions.assertContentEquals
import com.arnyminerz.escalaralcoiaicomtat.backend.server.error.Error
import com.arnyminerz.escalaralcoiaicomtat.backend.server.response.respondFailure
import com.arnyminerz.escalaralcoiaicomtat.backend.server.response.respondSuccess
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.json
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.jsonOf
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TestResponseFunctions {
    @Test
    fun `test respondFailure`() = testApplication {
        application {
            routing {
                get("/") {
                    respondFailure(
                        Error(0, "This is a testing error", HttpStatusCode.BadGateway)
                    )
                }
            }
        }
        client.get("/").apply {
            assertEquals(HttpStatusCode.BadGateway, status)
            val body = bodyAsText().json
            assertFalse(body.getBoolean("success"))
            assertContentEquals(
                jsonOf("code" to 0, "message" to "This is a testing error"),
                body.getJSONObject("error")
            )
        }
    }

    @Test
    fun `test respondSuccess`() = testApplication {
        application {
            routing {
                get("/") {
                    respondSuccess()
                }
                get("/accepted") {
                    respondSuccess(httpStatusCode = HttpStatusCode.Accepted)
                }
                get("/data") {
                    respondSuccess(data = jsonOf("test" to "value"))
                }
            }
        }
        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, status)
            val body = bodyAsText().json
            assertTrue(body.getBoolean("success"))
        }
        client.get("/accepted").apply {
            assertEquals(HttpStatusCode.Accepted, status)
            val body = bodyAsText().json
            assertTrue(body.getBoolean("success"))
        }
        client.get("/data").apply {
            assertEquals(HttpStatusCode.OK, status)
            val body = bodyAsText().json
            assertTrue(body.getBoolean("success"))
            assertContentEquals(jsonOf("test" to "value"), body.getJSONObject("data"))
        }
    }
}
