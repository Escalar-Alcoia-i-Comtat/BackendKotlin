package com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.query

import com.arnyminerz.escalaralcoiaicomtat.backend.ServerDatabase
import com.arnyminerz.escalaralcoiaicomtat.backend.database.entity.Path
import com.arnyminerz.escalaralcoiaicomtat.backend.localization.Localization
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.EndpointBase
import com.arnyminerz.escalaralcoiaicomtat.backend.server.error.Errors
import com.arnyminerz.escalaralcoiaicomtat.backend.server.response.respondFailure
import com.arnyminerz.escalaralcoiaicomtat.backend.server.response.respondSuccess
import io.ktor.http.HttpHeaders
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.plugins.ParameterConversionException
import io.ktor.server.request.header
import io.ktor.server.response.header
import io.ktor.server.util.getValue
import io.ktor.util.pipeline.PipelineContext

object PathEndpoint : EndpointBase() {
    override suspend fun PipelineContext<Unit, ApplicationCall>.endpoint() {
        val pathId = try {
            val pathId: Int by call.parameters
            pathId
        } catch (_: ParameterConversionException) {
            return respondFailure(Errors.InvalidData)
        }

        val language = call.request.header(HttpHeaders.AcceptLanguage)

        val path = ServerDatabase.instance.query { Path.findById(pathId) }
            ?: return respondFailure(Errors.ObjectNotFound)
        val pathJson = ServerDatabase.instance.query { path.toJson() }

        // If language requested, try loading translation from Crowdin
        if (language != null) {
            val otherDescription = Localization.getPathDescription(path, language)
            if (otherDescription != null) {
                // There's a translation for description
                pathJson.put("description", otherDescription)
                call.response.header(HttpHeaders.ContentLanguage, language)
            }
        }

        respondSuccess(pathJson)
    }
}
