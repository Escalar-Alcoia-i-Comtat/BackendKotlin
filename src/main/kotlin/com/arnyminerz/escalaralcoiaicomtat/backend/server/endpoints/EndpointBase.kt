package com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints

import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.request.receiveMultipart
import io.ktor.util.pipeline.PipelineContext

abstract class EndpointBase {
    suspend fun call(context: PipelineContext<Unit, ApplicationCall>) {
        with(context) { endpoint() }
    }

    /**
     * Receives a multipart request and processes each part of the request.
     *
     * @param forEachFormItem Callback function to handle each form item part.
     * @param forEachFileItem Callback function to handle each file item part.
     */
    protected suspend fun PipelineContext<Unit, ApplicationCall>.receiveMultipart(
        forEachFormItem: (suspend (partData: PartData.FormItem) -> Unit)? = null,
        forEachFileItem: (suspend (partData: PartData.FileItem) -> Unit)? = null
    ) {
        val multipart = call.receiveMultipart()

        multipart.forEachPart { partData ->
            when (partData) {
                is PartData.FormItem -> forEachFormItem?.invoke(partData)
                is PartData.FileItem -> forEachFileItem?.invoke(partData)
                else -> Unit
            }
        }
    }

    protected abstract suspend fun PipelineContext<Unit, ApplicationCall>.endpoint()
}
