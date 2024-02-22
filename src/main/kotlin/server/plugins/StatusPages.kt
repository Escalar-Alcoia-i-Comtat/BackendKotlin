package server.plugins

import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.statuspages.StatusPagesConfig
import server.error.Errors
import server.response.respondFailure

fun StatusPagesConfig.configureStatusPages() {
    exception<Throwable> { call, cause ->
        call.respondFailure(cause)
    }
    status(HttpStatusCode.NotFound) { call, _ ->
        call.respondFailure(Errors.EndpointNotFound)
    }
}
