package server.endpoints.query

import ServerDatabase
import database.entity.Path
import io.ktor.http.HttpHeaders
import io.ktor.server.plugins.ParameterConversionException
import io.ktor.server.request.header
import io.ktor.server.response.header
import io.ktor.server.routing.RoutingContext
import io.ktor.server.util.getValue
import localization.Localization
import server.endpoints.EndpointBase
import server.error.Errors
import server.response.ResourceId
import server.response.ResourceType
import server.response.respondFailure
import server.response.respondSuccess

object PathEndpoint : EndpointBase("/path/{pathId}") {
    override suspend fun RoutingContext.endpoint() {
        val pathId = try {
            val pathId: Int by call.parameters
            pathId
        } catch (_: ParameterConversionException) {
            return respondFailure(Errors.InvalidData)
        }

        val language = call.request.header(HttpHeaders.AcceptLanguage)

        val path = ServerDatabase.instance.query { Path.findById(pathId) }
            ?: return respondFailure(Errors.ObjectNotFound)

        // If language requested, try loading translation from Crowdin
        if (language != null) {
            val otherDescription = Localization.getPathDescription(path, language)
            if (otherDescription != null) {
                // There's a translation for description
                // call.response.header(HttpHeaders.ContentLanguage, language)
                // TODO: Set localized description
            }
        }

        call.response.header(HttpHeaders.ResourceType, "Path")
        call.response.header(HttpHeaders.ResourceId, pathId.toString())

        respondSuccess(path)
    }
}
