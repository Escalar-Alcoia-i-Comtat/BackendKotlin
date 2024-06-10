package server.base

import io.ktor.client.HttpClient

abstract class StubApplicationTestBuilder {
    abstract val client: HttpClient
}
