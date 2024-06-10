package server.base

import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType

abstract class StubApplicationTestBuilder(
    private val authToken: String
) {
    abstract val client: HttpClient

    suspend fun get(
        urlString: String,
        block: HttpRequestBuilder.() -> Unit = {}
    ) = client.get(urlString) {
        header(HttpHeaders.Authorization, "Bearer $authToken")
        block()
    }

    suspend fun post(
        urlString: String,
        block: HttpRequestBuilder.() -> Unit = {}
    ) = client.post(urlString) {
        header(HttpHeaders.Authorization, "Bearer $authToken")
        contentType(ContentType.Application.Json)
        block()
    }

    suspend fun patch(
        urlString: String,
        block: HttpRequestBuilder.() -> Unit = {}
    ) = client.patch(urlString) {
        header(HttpHeaders.Authorization, "Bearer $authToken")
        contentType(ContentType.Application.Json)
        block()
    }

    suspend fun delete(
        urlString: String,
        block: HttpRequestBuilder.() -> Unit = {}
    ) = client.delete(urlString) {
        header(HttpHeaders.Authorization, "Bearer $authToken")
        contentType(ContentType.Application.Json)
        block()
    }
}
