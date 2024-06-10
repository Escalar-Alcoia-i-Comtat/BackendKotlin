package server.endpoints

import Logger
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.readAllParts
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.request.receiveMultipart
import io.ktor.server.routing.Routing
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.util.pipeline.PipelineContext

abstract class EndpointModel(val endpoint: String) {
    companion object {
        fun Routing.delete(endpoint: EndpointModel) = delete(endpoint.endpoint) { endpoint.call(this) }

        fun Routing.get(endpoint: EndpointModel) = get(endpoint.endpoint) { endpoint.call(this) }

        fun Routing.patch(endpoint: EndpointModel) = patch(endpoint.endpoint) { endpoint.call(this) }

        fun Routing.post(endpoint: EndpointModel) = post(endpoint.endpoint) { endpoint.call(this) }
    }

    protected val rawMultipartFormItems = mutableMapOf<String, Any>()

    /**
     * Runs the endpoint's logic on the given [context].
     */
    abstract suspend fun call(context: PipelineContext<Unit, ApplicationCall>)

    /**
     * Receives a multipart request and processes each part of the request.
     *
     * @param forEachFormItem Callback function to handle each form item part.
     * @param forEachFileItem Callback function to handle each file item part.
     */
    protected suspend inline fun PipelineContext<Unit, ApplicationCall>.receiveMultipart(
        forEachFormItem: (partData: PartData.FormItem) -> Unit,
        forEachFileItem: (partData: PartData.FileItem) -> Unit
    ) {
        val multipart = call.receiveMultipart()

        for (partData in multipart.readAllParts()) {
            when (partData) {
                is PartData.FormItem -> {
                    val name = partData.name
                    if (name == null) {
                        Logger.warn("Received FormItem without a name. Value: ${partData.value}")
                    } else {
                        rawMultipartFormItems[name] = partData.value
                    }

                    forEachFormItem(partData)
                }

                is PartData.FileItem -> forEachFileItem(partData)
                else -> Unit
            }
        }
    }

    /**
     * Provides the logic of the endpoint, and should answer the call by itself. It's called by [call] when ready.
     */
    protected abstract suspend fun PipelineContext<Unit, ApplicationCall>.endpoint()
}
