package server.responses

import database.serialization.Json
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.serialization.Serializable
import server.error.Error
import server.plugins.installPlugins
import server.response.FailureResponse
import server.response.ResponseData
import server.response.SuccessResponse
import server.response.respondFailure
import server.response.respondSuccess

class TestResponseFunctions {
    @Serializable
    data class TestPairData(val key: String, val value: String): ResponseData

    @Test
    fun `test respondFailure`() = testApplication {
        application {
            installPlugins()
            routing {
                get("/") {
                    respondFailure(
                        Error(0, "This is a testing error", HttpStatusCode.BadGateway)
                    )
                }
            }
        }
        val client = createClient {
            install(ContentNegotiation) { json(Json) }
        }

        client.get("/").apply {
            assertEquals(HttpStatusCode.BadGateway, status)
            val body = body<FailureResponse>()
            assertFalse(body.success)
            assertEquals(0, body.error.code)
            assertEquals("This is a testing error", body.error.message)
        }
    }

    @Test
    fun `test respondSuccess`() = testApplication {
        application {
            installPlugins()
            routing {
                get("/") { respondSuccess() }
                get("/accepted") { respondSuccess(httpStatusCode = HttpStatusCode.Accepted) }
                get("/data") { respondSuccess(data = TestPairData("key", "value")) }
            }
        }
        val client = createClient {
            install(ContentNegotiation) { json(Json) }
        }

        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, status)
            val body = body<SuccessResponse<ResponseData>>()
            assertTrue(body.success)
        }
        client.get("/accepted").apply {
            assertEquals(HttpStatusCode.Accepted, status)
            val body = body<SuccessResponse<ResponseData>>()
            assertTrue(body.success)
        }
        client.get("/data").apply {
            assertEquals(HttpStatusCode.OK, status, "Status code is not OK. Body: ${bodyAsText()}")
            val body = body<SuccessResponse<TestPairData>>()
            assertTrue(body.success)
            assertEquals("key", body.data?.key)
            assertEquals("value", body.data?.value)
        }
    }
}
