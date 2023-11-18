package com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints

import com.arnyminerz.escalaralcoiaicomtat.backend.diagnostics.Performance
import com.arnyminerz.escalaralcoiaicomtat.backend.server.error.Errors
import com.arnyminerz.escalaralcoiaicomtat.backend.server.response.respondFailure
import com.arnyminerz.escalaralcoiaicomtat.backend.system.EnvironmentVariables
import io.ktor.http.HttpHeaders
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.request.header
import io.ktor.util.pipeline.PipelineContext

abstract class SecureEndpointBase(endpoint: String) : EndpointModel(endpoint) {
    companion object {
        /**
         * Represents the keyword used for Bearer token authentication.
         *
         * The value of this constant is "Bearer ".
         */
        private const val BEARER_KEYWORD = "Bearer "
    }

    override suspend fun call(context: PipelineContext<Unit, ApplicationCall>) =
        Performance.measure("SecureEndpointBase", endpoint) {
            with(context) {
                val authToken = EnvironmentVariables.Authentication.AuthToken.value
                    ?: // If there's no defined auth token. Disable all secure endpoints.
                    return respondFailure(Errors.AuthenticationDisabled)

                // Authorize the request
                val authorization = call.request.header(HttpHeaders.Authorization)
                    ?: return respondFailure(Errors.AuthenticationRequired)

                if (!authorization.startsWith(BEARER_KEYWORD))
                    return respondFailure(Errors.AuthenticationInvalid)

                val token = authorization.substring(BEARER_KEYWORD.length)
                if (token != authToken)
                    return respondFailure(Errors.AuthenticationFailed)

                endpoint()
            }
        }
}
