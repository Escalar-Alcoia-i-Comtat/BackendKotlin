package com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.query

import com.arnyminerz.escalaralcoiaicomtat.backend.ServerDatabase
import com.arnyminerz.escalaralcoiaicomtat.backend.database.entity.Area
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.EndpointBase
import com.arnyminerz.escalaralcoiaicomtat.backend.server.error.Errors
import com.arnyminerz.escalaralcoiaicomtat.backend.server.response.respondFailure
import com.arnyminerz.escalaralcoiaicomtat.backend.server.response.respondSuccess
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.plugins.ParameterConversionException
import io.ktor.server.util.getValue
import io.ktor.util.pipeline.PipelineContext

object AreaEndpoint : EndpointBase() {
    override suspend fun PipelineContext<Unit, ApplicationCall>.endpoint() {
        val areaId = try {
            val areaId: Int by call.parameters
            areaId
        } catch (_: ParameterConversionException) {
            return respondFailure(Errors.InvalidData)
        }

        val area = ServerDatabase.instance.query { Area.findById(areaId) }
            ?: return respondFailure(Errors.ObjectNotFound)

        respondSuccess(area.toJson())
    }
}
