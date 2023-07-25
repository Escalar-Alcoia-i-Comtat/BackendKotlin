package com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.files

import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.EndpointBase
import com.arnyminerz.escalaralcoiaicomtat.backend.server.error.Errors
import com.arnyminerz.escalaralcoiaicomtat.backend.server.response.respondFailure
import com.arnyminerz.escalaralcoiaicomtat.backend.storage.Storage
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.response.respondFile
import io.ktor.server.util.getValue
import io.ktor.util.pipeline.PipelineContext

object DownloadFileEndpoint : EndpointBase() {
    override suspend fun PipelineContext<Unit, ApplicationCall>.endpoint() {
        val uuid: String by call.parameters

        val file = Storage.find(uuid) ?: return respondFailure(Errors.FileNotFound)
        call.respondFile(file)
    }
}
