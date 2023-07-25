package com.arnyminerz.escalaralcoiaicomtat.backend.server.plugins

import com.arnyminerz.escalaralcoiaicomtat.backend.server.error.Errors
import com.arnyminerz.escalaralcoiaicomtat.backend.server.response.respondFailure
import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.statuspages.StatusPagesConfig

fun StatusPagesConfig.configureStatusPages() {
    exception<Throwable> { call, cause ->
        call.respondFailure(cause)
    }
    status(HttpStatusCode.NotFound) { call, _ ->
        call.respondFailure(Errors.EndpointNotFound)
    }
}
